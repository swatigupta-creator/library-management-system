package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.DashboardStatsResponse;
import com.library.dto.LoanIssueRequest;
import com.library.entity.Loan;
import com.library.entity.Member;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.UnauthorizedException;
import com.library.repository.MemberRepository;
import com.library.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final MemberRepository memberRepository;

    public LoanController(LoanService loanService, MemberRepository memberRepository) {
        this.loanService = loanService;
        this.memberRepository = memberRepository;
    }

    private void checkLibrarianAuth(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(AuthController.SESSION_AUTH_KEY))) {
            throw new UnauthorizedException("Unauthorized: Librarian login required");
        }
    }

    private Member resolveStudent(HttpSession session, HttpServletRequest request) {
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE_KEY);
        Long memberId = (Long) session.getAttribute(AuthController.SESSION_MEMBER_ID_KEY);
        if ("STUDENT".equals(role) && memberId != null) {
            return memberRepository.findById(memberId)
                    .filter(Member::isActive)
                    .orElseThrow(() -> new UnauthorizedException("Student account is inactive or not found"));
        }

        // Restore from X-Student-Id header
        String headerId = request.getHeader("X-Student-Id");
        if (headerId != null && !headerId.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(headerId.trim());
                Member member = memberRepository.findById(id).orElse(null);
                if (member != null && member.isActive()) {
                    session.setAttribute(AuthController.SESSION_ROLE_KEY, "STUDENT");
                    session.setAttribute(AuthController.SESSION_MEMBER_ID_KEY, member.getId());
                    session.setAttribute(AuthController.SESSION_USER_KEY, member.getName());
                    session.setAttribute(AuthController.SESSION_EMAIL_KEY, member.getEmail());
                    return member;
                }
            } catch (NumberFormatException ignored) {}
        }

        // Restore from X-Student-Email header
        String headerEmail = request.getHeader("X-Student-Email");
        if (headerEmail != null && !headerEmail.trim().isEmpty()) {
            Member member = memberRepository.findByEmailIgnoreCase(headerEmail.trim()).orElse(null);
            if (member != null && member.isActive()) {
                session.setAttribute(AuthController.SESSION_ROLE_KEY, "STUDENT");
                session.setAttribute(AuthController.SESSION_MEMBER_ID_KEY, member.getId());
                session.setAttribute(AuthController.SESSION_USER_KEY, member.getName());
                session.setAttribute(AuthController.SESSION_EMAIL_KEY, member.getEmail());
                return member;
            }
        }

        throw new UnauthorizedException("Student login required to borrow a book");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Loan>>> getAllLoans(
            @RequestParam(value = "status", required = false) String status,
            HttpSession session) {
        checkLibrarianAuth(session);
        List<Loan> loans = loanService.getAllLoans(status);
        return ResponseEntity.ok(ApiResponse.ok("Loans fetched successfully", loans));
    }

    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<Loan>> issueBook(
            @Valid @RequestBody LoanIssueRequest request,
            HttpSession session) {
        checkLibrarianAuth(session);
        Loan loan = loanService.issueBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book issued successfully to member", loan));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<ApiResponse<Loan>> returnBook(
            @PathVariable Long id,
            HttpSession session) {
        checkLibrarianAuth(session);
        Loan loan = loanService.returnBook(id);
        return ResponseEntity.ok(ApiResponse.ok("Book marked as returned successfully", loan));
    }

    @GetMapping("/my-loans")
    public ResponseEntity<ApiResponse<List<Loan>>> getMyLoans(
            HttpServletRequest request,
            HttpSession session) {
        Member student = resolveStudent(session, request);
        List<Loan> myLoans = loanService.getLoansByMemberId(student.getId());
        return ResponseEntity.ok(ApiResponse.ok("Student borrowed books retrieved", myLoans));
    }

    @PostMapping("/student/borrow/{bookId}")
    public ResponseEntity<ApiResponse<Loan>> studentBorrowBook(
            @PathVariable Long bookId,
            HttpServletRequest request,
            HttpSession session) {
        Member student = resolveStudent(session, request);
        Loan loan = loanService.issueBook(bookId, student.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book borrowed successfully! Due in 14 days.", loan));
    }

    @PutMapping("/student/return/{loanId}")
    public ResponseEntity<ApiResponse<Loan>> studentReturnBook(
            @PathVariable Long loanId,
            HttpServletRequest request,
            HttpSession session) {
        Member student = resolveStudent(session, request);
        Loan loan = loanService.getLoansByMemberId(student.getId()).stream()
                .filter(l -> l.getId().equals(loanId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Loan record not found for your account"));
        Loan returned = loanService.returnBook(loan.getId());
        return ResponseEntity.ok(ApiResponse.ok("Book returned successfully! Copy restored to inventory.", returned));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = loanService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Statistics retrieved", stats));
    }
}
