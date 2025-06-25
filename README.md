# BookMatrix - My Modern Book Tracking & Resource Platform!

A robust, user-friendly Library Management System built with Java Swing and MySQL, designed for educational institutions and organizations to efficiently manage books, users, and library operations.

## Features
- **Role-Based Access:** Admin, staff, and student roles with tailored permissions.
- **User Registration & Approval:** New users (students/staff) require admin approval before accessing the system.
- **Book Management:** Add, update, delete, and search books with real-time availability tracking.
- **Issue & Return Workflow:** Seamless book issuing and returning, with automatic updates to inventory.
- **Request & Approval Process:** Students can request books, and staff/admin can approve or reject requests.
- **Secure Authentication:** Password protection and user management.
- **Modern Java Swing GUI:** Intuitive, tabbed interface for easy navigation.
- **MySQL Database Integration:** Robust data storage and retrieval using JDBC.

## Technologies Used
- **Java 8+** (Swing for GUI)
- **MySQL** (JDBC)
- **Maven** (Project management)

## Setup Instructions

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd LibraryManagement
```

### 2. Set Up the Database
- Create a MySQL database named `library`.
- Import the provided SQL schema or use the following to create tables:

```sql
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100),
  email VARCHAR(100) UNIQUE,
  user_type VARCHAR(20),
  password VARCHAR(100),
  status VARCHAR(20) DEFAULT 'pending'
);

CREATE TABLE books (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200),
  author VARCHAR(100),
  publisher VARCHAR(100),
  year INT,
  status VARCHAR(20),
  available INT
);

CREATE TABLE issues (
  id INT AUTO_INCREMENT PRIMARY KEY,
  book_id INT,
  user_id INT,
  issue_date DATE,
  return_date DATE,
  status VARCHAR(20),
  FOREIGN KEY (book_id) REFERENCES books(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  book_id INT,
  user_id INT,
  request_date DATE,
  status VARCHAR(20),
  return_date DATE,
  FOREIGN KEY (book_id) REFERENCES books(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

- Update your MySQL credentials in the DAO classes if needed.

### 3. Build and Run
```bash
cd LibraryManagement
mvn clean package
mvn exec:java -Dexec.mainClass="com.library.LibraryManagementGUI"
```

## Usage
- **Admin:**
  - Log in with your admin credentials (default: `admin@library.com` / `admin123`).
  - Approve or reject new user registrations in the Approvals tab.
  - Manage books, users, and oversee all library operations.
- **Staff/Student:**
  - Register for an account. Wait for admin approval before logging in.
  - Search for books, request issues, and manage your issued books.

## Screenshots
_Add screenshots of the main GUI tabs here for better presentation._

## License
This project is open-source and available for educational and demonstration purposes.

---

**Feel free to contribute or reach out for collaboration!** 
