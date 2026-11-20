package com.library.config;

import com.library.constant.LibraryConstants;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds initial sample records into the database ONLY if tables are empty.
 * Uses repository methods and entity constructors.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public DataInitializer(BookRepository bookRepository, MemberRepository memberRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() == 0) {
            bookRepository.save(new Book("Effective Java", "Joshua Bloch", "978-0134685991", "Programming", 5));
            bookRepository.save(new Book("Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering", 3));
            bookRepository.save(new Book("Java: The Complete Reference", "Herbert Schildt", "978-1260440232", "Education", 4));
            bookRepository.save(new Book("Design Patterns", "Erich Gamma", "978-0201633610", "Architecture", 2));
        }

        if (memberRepository.count() == 0) {
            memberRepository.save(new Member("Aarav Sharma", "aarav.sharma@example.com", "9876543210", LocalDate.now(), LibraryConstants.STATUS_ACTIVE, "student123"));
            memberRepository.save(new Member("Priya Patel", "priya.patel@example.com", "9812345678", LocalDate.now(), LibraryConstants.STATUS_ACTIVE, "student123"));
        }
    }
}
