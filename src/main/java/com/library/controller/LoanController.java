package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.DashboardStatsResponse;
import com.library.dto.LoanIssueRequest;
import com.library.entity.Loan;
import com.library.exception.UnauthorizedException;
import com.library.service.LoanService;
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

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    private void checkLibrarianAuth(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(AuthController.SESSION_AUTH_KEY))) {
            throw new UnauthorizedException("Unauthorized: Librarian login required");
        }
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
            @PathVariable String id,
            HttpSession session) {
        checkLibrarianAuth(session);
        Loan loan = loanService.returnBook(id);
        return ResponseEntity.ok(ApiResponse.ok("Book marked as returned successfully", loan));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = loanService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Statistics fetched", stats));
    }
}
