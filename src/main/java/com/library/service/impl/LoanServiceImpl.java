package com.library.service.impl;

import com.library.constant.LibraryConstants;
import com.library.dto.DashboardStatsResponse;
import com.library.dto.LoanIssueRequest;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.Member;
import com.library.exception.BookUnavailableException;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.service.LoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service Implementation demonstrating:
 * 1. Interfaces & Abstraction (Implements LoanService)
 * 2. Polymorphism & Method Overriding (@Override)
 * 3. Method Overloading (issueBook(LoanIssueRequest) vs issueBook(Long, Long))
 * 4. Encapsulation (Calls entity business methods: book.decrementAvailableCopies(), loan.markAsReturned())
 * 5. Collections & Generics (List<Loan>)
 * 6. Exception Handling (BookUnavailableException, ResourceNotFoundException, LibraryException)
 * 7. Loops, Conditions & Reusable Methods
 */
@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanServiceImpl(LoanRepository loanRepository,
                           BookRepository bookRepository,
                           MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    // Overloaded Method 1: Issue book using detailed DTO request
    @Override
    @Transactional
    public Loan issueBook(LoanIssueRequest req) {
        if (req == null) {
            throw new LibraryException("Loan issue request body must not be null");
        }

        // 1. Validate Member
        if (req.getMemberId() == null) {
            throw new LibraryException("Member ID is required");
        }
        Member member = memberRepository.findById(req.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + req.getMemberId() + " was not found"));

        if (!member.isActive()) {
            throw new LibraryException("Member '" + member.getName() + "' is inactive and cannot borrow books");
        }

        long activeLoans = loanRepository.countByMemberIdAndStatus(member.getId(), LibraryConstants.STATUS_ISSUED);
        if (activeLoans >= LibraryConstants.MAX_ACTIVE_LOANS_PER_MEMBER) {
            throw new LibraryException("Member has reached the maximum borrowing limit of " +
                    LibraryConstants.MAX_ACTIVE_LOANS_PER_MEMBER + " active books");
        }

        // 2. Validate Book
        if (req.getBookId() == null) {
            throw new LibraryException("Book ID is required");
        }
        Book book = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + req.getBookId() + " was not found"));

        if (!book.isAvailable()) {
            throw new BookUnavailableException("Cannot issue book '" + book.getTitle() + "'. All copies are currently issued.");
        }

        // 3. Update Book Inventory (Encapsulation logic)
        book.decrementAvailableCopies();
        bookRepository.save(book);

        // 4. Create and Persist Loan
        LocalDate issueDate = req.getIssueDate() != null ? req.getIssueDate() : LocalDate.now();
        LocalDate dueDate = req.getDueDate() != null ? req.getDueDate() : issueDate.plusDays(LibraryConstants.DEFAULT_LOAN_DAYS);

        // Condition check on dates
        if (dueDate.isBefore(issueDate)) {
            throw new LibraryException("Due date cannot be earlier than the issue date");
        }

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setIssueDate(issueDate);
        loan.setDueDate(dueDate);
        loan.setStatus(LibraryConstants.STATUS_ISSUED);
        loan.setNotes(req.getNotes() != null ? req.getNotes().trim() : null);

        return loanRepository.save(loan);
    }

    // Overloaded Method 2: Issue book with IDs and default borrowing duration
    @Override
    @Transactional
    public Loan issueBook(Long bookId, Long memberId) {
        LoanIssueRequest req = new LoanIssueRequest();
        req.setBookId(bookId);
        req.setMemberId(memberId);
        req.setIssueDate(LocalDate.now());
        req.setDueDate(LocalDate.now().plusDays(LibraryConstants.DEFAULT_LOAN_DAYS));
        return issueBook(req);
    }

    @Override
    @Transactional
    public Loan returnBook(Long loanId) {
        if (loanId == null) {
            throw new LibraryException("Loan ID must not be null");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan record with ID " + loanId + " was not found"));

        // Condition check: prevent duplicate return
        if (loan.isReturned()) {
            throw new LibraryException("Book '" + loan.getBook().getTitle() + "' was already returned on " + loan.getReturnDate());
        }

        // Encapsulation business method on Loan
        loan.markAsReturned(LocalDate.now());

        // Encapsulation business method on Book: restore inventory copy
        Book book = loan.getBook();
        if (book != null) {
            book.incrementAvailableCopies();
            bookRepository.save(book);
        }

        return loanRepository.save(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAllByOrderByIssueDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans(String status) {
        return getLoansByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getLoansByStatus(String status) {
        if (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status.trim())) {
            return getAllLoans();
        }
        return loanRepository.findByStatusOrderByIssueDateDesc(status.trim().toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getLoansByMemberId(Long memberId) {
        if (memberId == null) {
            return java.util.Collections.emptyList();
        }
        return loanRepository.findByMemberIdOrderByIssueDateDesc(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalBooks = bookRepository.count();
        long totalCopies = bookRepository.sumTotalCopies();
        long availableCopies = bookRepository.sumAvailableCopies();
        long issuedCopies = totalCopies - availableCopies;
        if (issuedCopies < 0) {
            issuedCopies = 0;
        }
        long totalMembers = memberRepository.count();
        long activeLoans = loanRepository.countByStatus(LibraryConstants.STATUS_ISSUED);

        return new DashboardStatsResponse(
                totalBooks,
                totalCopies,
                availableCopies,
                issuedCopies,
                totalMembers,
                activeLoans
        );
    }
}
