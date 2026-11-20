package com.library.entity;

import com.library.constant.LibraryConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "members")
public class Member extends Person {

    @Column(nullable = false)
    private LocalDate membershipDate;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false, length = 100)
    private String password = "password123";

    public Member() {
        super();
        this.membershipDate = LocalDate.now();
        this.status = LibraryConstants.STATUS_ACTIVE;
        this.password = "password123";
    }

    public Member(String name, String email, String phone) {
        super(name, email, phone);
        this.membershipDate = LocalDate.now();
        this.status = LibraryConstants.STATUS_ACTIVE;
        this.password = "password123";
    }

    public Member(String name, String email, String phone, String password) {
        super(name, email, phone);
        this.membershipDate = LocalDate.now();
        this.status = LibraryConstants.STATUS_ACTIVE;
        this.password = (password != null && !password.trim().isEmpty()) ? password.trim() : "password123";
    }

    public Member(String name, String email, String phone, LocalDate membershipDate, String status, String password) {
        super(name, email, phone);
        this.membershipDate = membershipDate != null ? membershipDate : LocalDate.now();
        this.status = status != null ? status.trim().toUpperCase() : LibraryConstants.STATUS_ACTIVE;
        this.password = (password != null && !password.trim().isEmpty()) ? password.trim() : "password123";
    }

    @Override
    public String getRoleDescription() {
        return "Student / Library Member (Status: " + this.status + ", Joined: " + this.membershipDate + ")";
    }

    public boolean isActive() {
        return LibraryConstants.STATUS_ACTIVE.equalsIgnoreCase(this.status);
    }

    public LocalDate getMembershipDate() {
        return membershipDate;
    }

    public void setMembershipDate(LocalDate membershipDate) {
        this.membershipDate = membershipDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim().toUpperCase() : LibraryConstants.STATUS_ACTIVE;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = (password != null && !password.trim().isEmpty()) ? password.trim() : "password123";
    }

    @Override
    public String toString() {
        return "Member{id=" + id + ", name='" + name + "', email='" + email + "', status='" + status + "'}";
    }
}
