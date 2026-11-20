package com.library.service.impl;

import com.library.constant.LibraryConstants;
import com.library.dto.BookRequest;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation demonstrating:
 * 1. Interfaces & Abstraction (Implements BookService interface)
 * 2. Polymorphism & Method Overriding (@Override on interface methods)
 * 3. Method Overloading (getAllBooks() vs getAllBooks(String search))
 * 4. Collections & Generics (List<Book>)
 * 5. Loops, Conditions, Methods & Strings (Validation, trimming, string matching)
 * 6. Access Control and Final (Final dependencies with constructor injection)
 * 7. Exception Handling (ResourceNotFoundException, LibraryException)
 */
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    // Constructor Injection (Best practice: final dependencies)
    public BookServiceImpl(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    // Overloaded Method 1: Get all books
    @Override
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Overloaded Method 2: Get books filtered by search query
    @Override
    @Transactional(readOnly = true)
    public List<Book> getAllBooks(String search) {
        // String handling & conditional checks
        if (search == null || search.trim().isEmpty()) {
            return getAllBooks();
        }
        String query = search.trim();
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                query, query, query);
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        if (id == null) {
            throw new LibraryException("Book ID must not be null");
        }
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " was not found in the library"));
    }

    @Override
    @Transactional
    public Book addBook(BookRequest req) {
        // Validation using String methods
        String cleanedIsbn = req.getIsbn() != null ? req.getIsbn().trim() : "";
        if (cleanedIsbn.isEmpty()) {
            throw new LibraryException("Book ISBN cannot be empty");
        }

        if (bookRepository.existsByIsbn(cleanedIsbn)) {
            throw new LibraryException("A book with ISBN '" + cleanedIsbn + "' is already registered in the library");
        }

        int total = req.getTotalCopies() != null ? req.getTotalCopies() : 1;
        if (total < 1) {
            throw new LibraryException("Total copies must be at least 1");
        }

        // Creating Book object using overloaded constructor
        Book book = new Book(
                req.getTitle().trim(),
                req.getAuthor().trim(),
                cleanedIsbn,
                req.getCategory() != null ? req.getCategory().trim() : "General",
                total
        );

        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public Book updateBook(Long id, BookRequest req) {
        Book existing = getBookById(id);

        String cleanedIsbn = req.getIsbn() != null ? req.getIsbn().trim() : "";
        if (bookRepository.existsByIsbnAndIdNot(cleanedIsbn, id)) {
            throw new LibraryException("Another book is already registered with ISBN: " + cleanedIsbn);
        }

        // Inventory consistency calculations
        int currentlyIssued = existing.getTotalCopies() - existing.getAvailableCopies();
        int newTotal = req.getTotalCopies() != null ? req.getTotalCopies() : 1;

        if (newTotal < currentlyIssued) {
            throw new LibraryException("Cannot reduce total copies to " + newTotal +
                    ". There are currently " + currentlyIssued + " copies issued to members.");
        }

        int newAvailable = newTotal - currentlyIssued;

        // Encapsulation: updating fields via setters
        existing.setTitle(req.getTitle().trim());
        existing.setAuthor(req.getAuthor().trim());
        existing.setIsbn(cleanedIsbn);
        existing.setCategory(req.getCategory() != null ? req.getCategory().trim() : "General");
        existing.setTotalCopies(newTotal);
        existing.setAvailableCopies(newAvailable);

        return bookRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = getBookById(id);

        // Loops & conditions: Verify no active loans exist before allowing deletion
        List<Loan> activeLoans = loanRepository.findByStatusOrderByIssueDateDesc(LibraryConstants.STATUS_ISSUED);
        for (Loan loan : activeLoans) {
            if (loan.getBook() != null && loan.getBook().getId().equals(book.getId())) {
                throw new LibraryException("Cannot delete book '" + book.getTitle() +
                        "' because one or more copies are currently issued to members.");
            }
        }

        bookRepository.delete(book);
    }
}
