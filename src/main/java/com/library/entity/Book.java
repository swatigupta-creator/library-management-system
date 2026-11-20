package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = false, unique = true, length = 50)
    private String isbn;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    private Integer totalCopies;

    @Column(nullable = false)
    private Integer availableCopies;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Book() {
        this.totalCopies = 1;
        this.availableCopies = 1;
        this.createdAt = LocalDateTime.now();
    }

    public Book(String title, String author, String isbn) {
        this.title = title != null ? title.trim() : "";
        this.author = author != null ? author.trim() : "";
        this.isbn = isbn != null ? isbn.trim() : "";
        this.category = "General";
        this.totalCopies = 1;
        this.availableCopies = 1;
        this.createdAt = LocalDateTime.now();
    }

    public Book(String title, String author, String isbn, String category, int totalCopies) {
        this.title = title != null ? title.trim() : "";
        this.author = author != null ? author.trim() : "";
        this.isbn = isbn != null ? isbn.trim() : "";
        this.category = category != null ? category.trim() : "General";
        this.totalCopies = Math.max(1, totalCopies);
        this.availableCopies = this.totalCopies;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isAvailable() {
        return this.availableCopies != null && this.availableCopies > 0;
    }

    public void decrementAvailableCopies() {
        if (!isAvailable()) {
            throw new IllegalStateException("Cannot issue book '" + this.title + "': No copies currently available");
        }
        this.availableCopies--;
    }

    public void incrementAvailableCopies() {
        if (this.availableCopies != null && this.totalCopies != null && this.availableCopies < this.totalCopies) {
            this.availableCopies++;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author != null ? author.trim() : null;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn != null ? isbn.trim() : null;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category != null ? category.trim() : null;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Book{id=" + id + ", title='" + title + "', author='" + author + "', isbn='" + isbn + "', available=" + availableCopies + "/" + totalCopies + "}";
    }
}
