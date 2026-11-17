package com.library.service;

import com.library.dto.DashboardStatsResponse;
import com.library.dto.LoanIssueRequest;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.Member;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository, MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    public List<Loan> getAllLoans(String status) {
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            return loanRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase().trim());
        }
        return loanRepository.findAllByOrderByCreatedAtDesc();
    }

    public Loan issueBook(LoanIssueRequest req) {
        Book book = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + req.getBookId() + " not found"));

        Member member = memberRepository.findById(req.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + req.getMemberId() + " not found"));

        if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            throw new BadRequestException("Member account is not active");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("Book '" + book.getTitle() + "' is out of stock (no available copies to issue)");
        }

        LocalDate issueDate = req.getIssueDate() != null ? req.getIssueDate() : LocalDate.now();
        if (req.getDueDate().isBefore(issueDate)) {
            throw new BadRequestException("Due date cannot be before issue date");
        }

        // Decrement book availability in MongoDB
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Create Loan document
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setIssueDate(issueDate);
        loan.setDueDate(req.getDueDate());
        loan.setStatus("ISSUED");
        loan.setNotes(req.getNotes());

        return loanRepository.save(loan);
    }

    public Loan returnBook(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan record with ID " + loanId + " not found"));

        if ("RETURNED".equalsIgnoreCase(loan.getStatus())) {
            throw new BadRequestException("This book has already been marked as returned on " + loan.getReturnDate());
        }

        loan.setStatus("RETURNED");
        loan.setReturnDate(LocalDate.now());

        // Increment book availability in MongoDB
        Book book = loan.getBook();
        if (book != null) {
            bookRepository.findById(book.getId()).ifPresent(dbBook -> {
                if (dbBook.getAvailableCopies() < dbBook.getTotalCopies()) {
                    dbBook.setAvailableCopies(dbBook.getAvailableCopies() + 1);
                    bookRepository.save(dbBook);
                    loan.setBook(dbBook);
                }
            });
        }

        return loanRepository.save(loan);
    }

    public DashboardStatsResponse getDashboardStats() {
        List<Book> allBooks = bookRepository.findAll();
        long totalBooks = allBooks.size();
        long totalCopies = allBooks.stream().mapToLong(b -> b.getTotalCopies() != null ? b.getTotalCopies() : 0).sum();
        long availableCopies = allBooks.stream().mapToLong(b -> b.getAvailableCopies() != null ? b.getAvailableCopies() : 0).sum();
        long issuedCopies = Math.max(0, totalCopies - availableCopies);
        long totalMembers = memberRepository.count();
        long activeLoans = loanRepository.countByStatus("ISSUED");

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
