# 📚 Library Management System (Spring Boot + MongoDB + HTML/CSS/JS)

Simple aur realistic Library Management System website jisme Java Spring Boot backend, Spring Data MongoDB, MongoDB database (`library_db`), aur responsive frontend (HTML, CSS, JavaScript) shamil hai.

---

## 🌐 Website URLs (Local Links)

Jab backend run ho jayega, aap browser mein in links par website open kar sakte hain:

| Page | URL | Description |
| :--- | :--- | :--- |
| **🏠 Home Page** | [http://localhost:8080/](http://localhost:8080/) | Library ka naam, navigation, live inventory stats aur key highlights |
| **📖 Browse Books** | [http://localhost:8080/books.html](http://localhost:8080/books.html) | Database books catalog, title/author live search, aur real-time availability badges |
| **🔐 Librarian Login** | [http://localhost:8080/login.html](http://localhost:8080/login.html) | Librarian sign-in page |
| **⚙️ Librarian Portal** | [http://localhost:8080/librarian.html](http://localhost:8080/librarian.html) | Books CRUD, Members management, Issue & Return Loan tracker |

### 🔑 Default Librarian Credentials
- **Username:** `librarian`
- **Password:** `admin123`

---

## 🍃 MongoDB Database Setup (`library_db`)

Database ka naam: `library_db`

### Tareeqa 1: Automatic Creation (Zero Setup)
Spring Boot jab start hota hai aur pehla record save hota hai, MongoDB automatically `library_db` database aur `books`, `members`, `loans` collections create kar deta hai!

### Tareeqa 2: MongoDB Compass (GUI) Se Setup
1. **MongoDB Compass** open karein aur `mongodb://localhost:27017` connect karein.
2. **Create Database** button par click karein:
   - Database Name: `library_db`
   - Collection Name: `books`
3. Uske baad do aur collections add karein:
   - `members`
   - `loans`

### Tareeqa 3: `mongosh` / Terminal Se Setup
Terminal mein run karein:
```javascript
use library_db;
db.createCollection("books");
db.createCollection("members");
db.createCollection("loans");
```
*(Ya aap project root mein di gayi `mongo-init.js` script run kar sakte hain).*

---

## ⚙️ MongoDB Connection (`application.properties`)

Aapka [application.properties](file:///C:/Users/Swati/.gemini/antigravity/scratch/library-management-system/src/main/resources/application.properties) default local MongoDB se configured hai:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/library_db
```
*(Agar aap MongoDB Atlas cloud cluster use kar rahe hain toh bas Atlas ki connection string yahan paste karein).*

---

## 🚀 Run Backend
1. **IntelliJ IDEA** mein project folder open karein:
   `C:\Users\Swati\.gemini\antigravity\scratch\library-management-system`
2. `LibraryManagementApplication.java` open karein aur **Run ▶** par click karein!
*(Ya double-click karein [run-server.bat](file:///C:/Users/Swati/.gemini/antigravity/scratch/library-management-system/run-server.bat) par).*
