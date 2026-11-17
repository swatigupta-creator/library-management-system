package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.BookRequest;
import com.library.entity.Book;
import com.library.exception.UnauthorizedException;
import com.library.service.BookService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    private void checkLibrarianAuth(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(AuthController.SESSION_AUTH_KEY))) {
            throw new UnauthorizedException("Unauthorized: Librarian login required");
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks(@RequestParam(value = "search", required = false) String search) {
        List<Book> books = bookService.getAllBooks(search);
        return ResponseEntity.ok(ApiResponse.ok("Books fetched successfully", books));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable String id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.ok("Book details fetched", book));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Book>> addBook(
            @Valid @RequestBody BookRequest request,
            HttpSession session) {
        checkLibrarianAuth(session);
        Book created = bookService.addBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book added successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> updateBook(
            @PathVariable String id,
            @Valid @RequestBody BookRequest request,
            HttpSession session) {
        checkLibrarianAuth(session);
        Book updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Book updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable String id,
            HttpSession session) {
        checkLibrarianAuth(session);
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.ok("Book deleted successfully"));
    }
}
