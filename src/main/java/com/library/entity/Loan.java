package com.library.entity;

import com.library.constant.LibraryConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnDate;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 255)
    private String notes;

    public Loan() {
        this.issueDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(LibraryConstants.DEFAULT_LOAN_DAYS);
        this.status = LibraryConstants.STATUS_ISSUED;
    }

    public Loan(Book book, Member member) {
        this.book = book;
        this.member = member;
        this.issueDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(LibraryConstants.DEFAULT_LOAN_DAYS);
        this.status = LibraryConstants.STATUS_ISSUED;
    }

    public Loan(Book book, Member member, int durationDays, String notes) {
        this.book = book;
        this.member = member;
        this.issueDate = LocalDate.now();
        int validDays = durationDays > 0 ? durationDays : LibraryConstants.DEFAULT_LOAN_DAYS;
        this.dueDate = LocalDate.now().plusDays(validDays);
        this.status = LibraryConstants.STATUS_ISSUED;
        this.notes = notes != null ? notes.trim() : null;
    }

    public void markAsReturned(LocalDate date) {
        this.returnDate = (date != null) ? date : LocalDate.now();
        this.status = LibraryConstants.STATUS_RETURNED;
    }

    public boolean isReturned() {
        return LibraryConstants.STATUS_RETURNED.equalsIgnoreCase(this.status);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim().toUpperCase() : LibraryConstants.STATUS_ISSUED;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes.trim() : null;
    }

    @Override
    public String toString() {
        return "Loan{id=" + id + ", book=" + (book != null ? book.getTitle() : "null") +
                ", member=" + (member != null ? member.getName() : "null") +
                ", status='" + status + "', dueDate=" + dueDate + "}";
    }
}
