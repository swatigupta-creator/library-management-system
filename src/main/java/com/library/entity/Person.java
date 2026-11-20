package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * Abstract Base Class demonstrating:
 * 1. Inheritance (Base class for human records like Member)
 * 2. Abstraction (Abstract class with abstract method getRoleDescription)
 * 3. Encapsulation (Protected fields accessed via getters/setters)
 * 4. Constructors & Method Overloading
 */
@MappedSuperclass
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false, length = 150)
    protected String name;

    @Column(nullable = false, unique = true, length = 150)
    protected String email;

    @Column(nullable = false, length = 50)
    protected String phone;

    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    // Default Constructor (Required by JPA)
    public Person() {
        this.createdAt = LocalDateTime.now();
    }

    // Overloaded Constructor with basic personal details
    public Person(String name, String email, String phone) {
        this.name = name != null ? name.trim() : null;
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.phone = phone != null ? phone.trim() : null;
        this.createdAt = LocalDateTime.now();
    }

    // Abstract method: Every concrete subclass MUST provide its own role description
    public abstract String getRoleDescription();

    // Getters and Setters (Encapsulation)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
