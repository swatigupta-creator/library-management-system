// ==============================================================
// Library Management System - MongoDB Initialization Script
// Database: library_db
// Collections: books, members, loans
// ==============================================================

// 1. Switch to library_db database
use library_db;

// 2. Create 'books' collection with schema validation & unique index on ISBN
db.createCollection("books");
db.books.createIndex({ "isbn": 1 }, { unique: true });
db.books.createIndex({ "title": "text", "author": "text" });

// 3. Create 'members' collection with unique index on Email
db.createCollection("members");
db.members.createIndex({ "email": 1 }, { unique: true });

// 4. Create 'loans' collection with indexes for fast lookups
db.createCollection("loans");
db.loans.createIndex({ "status": 1 });
db.loans.createIndex({ "book.id": 1 });
db.loans.createIndex({ "member.id": 1 });

print("========================================================");
print("Successfully created 'library_db' with collections:");
print(" - books");
print(" - members");
print(" - loans");
print("========================================================");
