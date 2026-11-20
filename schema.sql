-- ==============================================================
-- Library Management System Database Schema
-- Database: MySQL
-- Tables: books, members, loans
-- ==============================================================

CREATE DATABASE IF NOT EXISTS library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- 1. Table: books
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_total_copies CHECK (total_copies >= 0),
    CONSTRAINT chk_available_copies CHECK (available_copies >= 0 AND available_copies <= total_copies)
) ENGINE=InnoDB;

-- 2. Table: members (Inherits personal fields from Person model)
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL DEFAULT 'password123',
    membership_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Table: loans
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ISSUED',
    notes VARCHAR(255) NULL,
    CONSTRAINT fk_loan_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT,
    CONSTRAINT fk_loan_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ==============================================================
-- Optional Initial Sample Data for Immediate Testing
-- ==============================================================
INSERT IGNORE INTO books (id, title, author, isbn, category, total_copies, available_copies) VALUES
(1, 'Effective Java', 'Joshua Bloch', '978-0134685991', 'Programming', 5, 5),
(2, 'Clean Code', 'Robert C. Martin', '978-0132350884', 'Software Engineering', 3, 3),
(3, 'Java: The Complete Reference', 'Herbert Schildt', '978-1260440232', 'Education', 4, 4);

INSERT IGNORE INTO members (id, name, email, phone, membership_date, status) VALUES
(1, 'Aarav Sharma', 'aarav.sharma@example.com', '9876543210', CURDATE(), 'ACTIVE'),
(2, 'Priya Patel', 'priya.patel@example.com', '9812345678', CURDATE(), 'ACTIVE');
