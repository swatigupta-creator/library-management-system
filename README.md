# 📚 Library Management System (Spring Boot + MySQL + Spring Data JPA + HTML/CSS/JS)

Ek simple, realistic aur production-grade Library Management System website jo Java Spring Boot backend, Spring Data JPA, MySQL database (`library_db`), aur responsive frontend (HTML, CSS, JavaScript) ke sath bani hai.

---

## 🎯 Project Overview & Architecture

- **Backend:** Java 17+ with Spring Boot 3.3.5
- **Database:** MySQL (Database: `library_db`)
- **Database Connectivity / ORM:** Spring Data JPA with Hibernate
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (Fetch API, responsive modern UI, no heavy frameworks)
- **Architecture Pattern:** Layered Architecture (Controller ➔ Service Interface ➔ Service Implementation ➔ Repository ➔ MySQL Database)

---

## 📑 Syllabus Concept Mapping

Is project ke working code mein Java ke Core aur Object-Oriented concepts ko naturally implement kiya gaya hai:

| # | Java Syllabus Concept | Relevant Class / File | Actual Role & Implementation in Project |
| :- | :--- | :--- | :--- |
| **1** | **Classes aur Objects** | `Book.java`, `Member.java`, `Loan.java` | Real-world library entities ko define karte hain. Inke objects create karke database records (books catalog, members list, loan issuance) ko handle kiya jata hai. |
| **2** | **Encapsulation** | `Person.java`, `Book.java`, `Member.java`, `Loan.java` | Saare entity fields `private` / `protected` hain. Access getters/setters aur meaningful business methods (`decrementAvailableCopies()`, `incrementAvailableCopies()`, `isAvailable()`, `markAsReturned()`) ke zariye diya gaya hai. |
| **3** | **Constructors & Method Overloading** | `Book.java`, `Member.java`, `Loan.java`, `BookService.java`, `LoanService.java` | **Constructors:** Default JPA constructor ke sath overloaded constructors (e.g. `Book(title, author, isbn)` aur `Book(title, author, isbn, category, totalCopies)`).<br>**Method Overloading:** `getAllBooks()` vs `getAllBooks(String keyword)`, aur `issueBook(req)` vs `issueBook(bookId, memberId)` naturally implement kiye gaye hain. |
| **4** | **Inheritance** | `Person.java` (Base class) ➔ `Member.java` (Subclass) | `Person` abstract class (`@MappedSuperclass`) personal fields (`id`, `name`, `email`, `phone`, `createdAt`) aur abstract method `getRoleDescription()` define karti hai. `Member` isse extend karke library-specific fields (`membershipDate`, `status`) add karta hai. |
| **5** | **Interfaces aur Abstraction** | `BookService.java`, `MemberService.java`, `LoanService.java`, `BookRepository.java` | Business contract service interfaces ke through define hai (`BookService`), jabki actual business logic implementation classes (`BookServiceImpl`) mein hai. Repositories Spring Data JPA ke generic `JpaRepository<T, ID>` interface ko extend karte hain. |
| **6** | **Polymorphism & Method Overriding** | `BookController.java`, `Member.java`, `*ServiceImpl.java` | **Runtime Polymorphism:** Controllers service interfaces ke reference variable ko hold karte hain (`private final BookService bookService;`), aur Spring runtime par concrete bean inject karta hai.<br>**Method Overriding:** Subclass `Member` base class ke `getRoleDescription()` aur `toString()` ko `@Override` karta hai; implementation classes interface methods ko `@Override` karti hain. |
| **7** | **Packages aur Imports** | `com.library.*` | Clean package separation: `controller`, `service`, `service.impl`, `repository`, `entity`, `dto`, `exception`, `constant`. Clean aur explicit imports use kiye gaye hain. |
| **8** | **Exception Handling** | `ResourceNotFoundException.java`, `BookUnavailableException.java`, `LibraryException.java`, `GlobalExceptionHandler.java` | Custom exceptions banayi gayi hain. `@RestControllerAdvice` centralized error handler mein 404 Not Found, 400 Bad Request, aur 500 Internal Server Error ko structured `ApiResponse` ke sath handle kiya gaya hai. Empty catch blocks kahin nahi hain. |
| **9** | **Collections aur Generics** | `ApiResponse<T>`, `BookRepository`, `LoanServiceImpl` | Java Collections (`List<Book>`, `List<Member>`, `List<Loan>`, `Map<String, String>`) aur Generics (`ApiResponse<T>`, `JpaRepository<Book, Long>`, `Optional<T>`) har repository aur service mein naturally use hue hain. |
| **10** | **Loops, Conditions, Methods aur Strings** | `BookServiceImpl.java`, `LoanServiceImpl.java` | Search filtering, ISBN/email trimming (`trim()`, `toLowerCase()`), available copies checks (`if (book.getAvailableCopies() <= 0)`), aur active loans validation mein loops aur conditional logic ka realistic use hai. |
| **11** | **Access Control aur Static/Final** | `LibraryConstants.java`, Controllers, Services | Access modifiers (`private`, `protected`, `public`) ka sahi use. `public static final` constants (e.g. `DEFAULT_LOAN_DAYS = 14`, `MAX_ACTIVE_LOANS_PER_MEMBER = 5`). Services aur controllers mein constructor injection ke liye `final` keywords ka use. |
| **12** | **Threads** | *Omitted (Not applicable)* | **Reason:** Simple CRUD web application mein manual `Thread` / `Runnable` banana anti-pattern hai. Spring Boot ka embedded Tomcat web container automatically request level par thread pool manage karta hai. |
| **13** | **AWT, Swing, Applets, Raw Sockets** | *Omitted (Not applicable)* | **Reason:** Yeh modern 3-tier web application hai jisme client-side HTML5/CSS3/JavaScript hai aur server-side Spring Boot REST APIs (HTTP/JSON) handle karta hai. Desktop GUI (AWT/Swing) ya Applets web context mein obsolete hain. |

---

## 🗄️ Database Schema (`library_db`)

Database mein sirf 3 zaroori tables hain:

### 1. `books`
- `id` (BIGINT, Primary Key, Auto Increment)
- `title` (VARCHAR(255), Not Null)
- `author` (VARCHAR(255), Not Null)
- `isbn` (VARCHAR(50), Not Null, Unique)
- `category` (VARCHAR(100))
- `total_copies` (INT, Not Null)
- `available_copies` (INT, Not Null)
- `created_at` (TIMESTAMP)

### 2. `members`
- `id` (BIGINT, Primary Key, Auto Increment)
- `name` (VARCHAR(150), Not Null)
- `email` (VARCHAR(150), Not Null, Unique)
- `phone` (VARCHAR(50), Not Null)
- `membership_date` (DATE, Not Null)
- `status` (VARCHAR(30), Default 'ACTIVE')
- `created_at` (TIMESTAMP)

### 3. `loans`
- `id` (BIGINT, Primary Key, Auto Increment)
- `book_id` (BIGINT, Foreign Key references `books(id)`)
- `member_id` (BIGINT, Foreign Key references `members(id)`)
- `issue_date` (DATE, Not Null)
- `due_date` (DATE, Not Null)
- `return_date` (DATE, Nullable)
- `status` (VARCHAR(30), 'ISSUED' or 'RETURNED')
- `notes` (VARCHAR(255), Nullable)

---

## 🌐 Pages & Navigation

Jab application run ho jaye, browser mein yeh pages open karein:

| Page | URL | Functionality |
| :--- | :--- | :--- |
| **🏠 Home Page** | `http://localhost:8080/index.html` | Library name, clean navigation, overview statistics (total books, available copies, members, active loans). |
| **📖 Browse Books** | `http://localhost:8080/books.html` | Database se books catalog view, live title/author/ISBN search, real-time availability badges (`Available (X of Y)` vs `All Issued`). |
| **🔐 Librarian Login** | `http://localhost:8080/login.html` | Librarian authentication page. |
| **⚙️ Librarian Portal** | `http://localhost:8080/librarian.html` | Books Management (Add, Edit, Delete, List), Members Management (Add, List), Loans Management (Issue Book, Return Book with auto inventory update). |

### 🔑 Default Librarian Credentials
- **Username:** `librarian`
- **Password:** `admin123`

---

## 🚀 Setup & Run Instructions

### Step 1: MySQL Database Setup
1. MySQL Workbench ya terminal mein MySQL open karein:
   ```bash
   mysql -u root -p
   ```
2. Database aur tables create karein:
   ```sql
   CREATE DATABASE IF NOT EXISTS library_db;
   USE library_db;
   ```
   *(Aap project ke root folder mein maujood `schema.sql` file ko bhi run kar sakte hain).*

3. Database connection check karein:
   [src/main/resources/application.properties](file:///C:/Users/Swati/.gemini/antigravity/scratch/library-management-system/src/main/resources/application.properties) mein apna MySQL username aur password verify karein:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   ```

---

### Step 2: Project Run Karne ke Tareeqe

#### Option A: IntelliJ IDEA ya Eclipse / VS Code se (Sabse Simple)
1. Apne IDE (IntelliJ IDEA / Eclipse / VS Code) mein project folder open karein:
   `C:\Users\Swati\.gemini\antigravity\scratch\library-management-system`
2. Maven project ko reload hone dein.
3. [LibraryManagementApplication.java](file:///C:/Users/Swati/.gemini/antigravity/scratch/library-management-system/src/main/java/com/library/LibraryManagementApplication.java) par right click karein aur **Run** click karein.

#### Option B: Terminal / Command Line se
Project folder mein terminal open karein aur run karein:
```bash
mvn spring-boot:run
```
*(Windows batch script `run-server.bat` par double click karke bhi start kar sakte hain).*

#### Option C: Instant In-Memory (H2 Profile) Testing (Agar MySQL abhi install/start na ho)
Agar aap bina MySQL start kiye turant test karna chahte hain:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
*(Ya `run-server-h2.bat` par double click karein).*

---

### Step 3: Browser mein Test Karein
Application start hone ke baad browser mein open karein:
- **Home:** [http://localhost:8080/](http://localhost:8080/)
- **Books Catalog:** [http://localhost:8080/books.html](http://localhost:8080/books.html)
- **Librarian Portal:** [http://localhost:8080/librarian.html](http://localhost:8080/librarian.html)
