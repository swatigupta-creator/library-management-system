package com.library.service;

import com.library.dto.BookRequest;
import com.library.entity.Book;

import java.util.List;

public interface BookService {

    List<Book> getAllBooks();

    List<Book> getAllBooks(String search);

    Book getBookById(Long id);

    Book addBook(BookRequest req);

    Book updateBook(Long id, BookRequest req);

    void deleteBook(Long id);
}
