package com.library.service;

import com.library.dto.BookRequest;
import com.library.entity.Book;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    public List<Book> getAllBooks(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return bookRepository.searchBooks(search.trim());
        }
        return bookRepository.findAll();
    }

    public Book getBookById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with ID " + id + " not found"));
    }

    public Book addBook(BookRequest req) {
        if (bookRepository.existsByIsbn(req.getIsbn().trim())) {
            throw new BadRequestException("A book with ISBN '" + req.getIsbn().trim() + "' already exists");
        }

        Book book = new Book();
        book.setTitle(req.getTitle().trim());
        book.setAuthor(req.getAuthor().trim());
        book.setIsbn(req.getIsbn().trim());
        book.setCategory(req.getCategory().trim());
        book.setTotalCopies(req.getTotalCopies());
        book.setAvailableCopies(req.getTotalCopies());

        return bookRepository.save(book);
    }

    public Book updateBook(String id, BookRequest req) {
        Book book = getBookById(id);

        // Check if ISBN changed and is taken by another book
        if (!book.getIsbn().equalsIgnoreCase(req.getIsbn().trim())) {
            bookRepository.findByIsbn(req.getIsbn().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BadRequestException("Another book already uses ISBN: " + req.getIsbn().trim());
                }
            });
        }

        int currentlyIssued = book.getTotalCopies() - book.getAvailableCopies();
        if (req.getTotalCopies() < currentlyIssued) {
            throw new BadRequestException("Cannot reduce total copies below currently issued count (" + currentlyIssued + ")");
        }

        int difference = req.getTotalCopies() - book.getTotalCopies();
        book.setAvailableCopies(book.getAvailableCopies() + difference);

        book.setTitle(req.getTitle().trim());
        book.setAuthor(req.getAuthor().trim());
        book.setIsbn(req.getIsbn().trim());
        book.setCategory(req.getCategory().trim());
        book.setTotalCopies(req.getTotalCopies());

        return bookRepository.save(book);
    }

    public void deleteBook(String id) {
        Book book = getBookById(id);

        boolean hasActiveLoans = loanRepository.existsByBookIdAndStatus(id, "ISSUED");
        if (hasActiveLoans) {
            throw new BadRequestException("Cannot delete book '" + book.getTitle() + "' because copies are currently issued to members");
        }

        List<com.library.entity.Loan> loans = loanRepository.findByBookId(id);
        if (!loans.isEmpty()) {
            throw new BadRequestException("Cannot delete book because loan transaction history exists for it");
        }

        bookRepository.delete(book);
    }
}
