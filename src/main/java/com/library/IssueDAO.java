package com.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public void addIssue(Issue issue) {
        // Check if book is available
        BookDAO bookDAO = new BookDAO();
        Book book = bookDAO.getBookById(issue.getBookId());
        if (book != null && book.getAvailable() > 0) {
            String sql = "INSERT INTO issues (book_id, user_id, issue_date, return_date, status) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, issue.getBookId());
                stmt.setInt(2, issue.getUserId());
                stmt.setDate(3, issue.getIssueDate());
                stmt.setDate(4, issue.getReturnDate());
                stmt.setString(5, issue.getStatus());
                stmt.executeUpdate();
                // Decrement available count
                book.setAvailable(book.getAvailable() - 1);
                bookDAO.updateBook(book);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Book is not available for issuing.");
        }
    }

    public List<Issue> getAllIssues() {
        List<Issue> issues = new ArrayList<>();
        String sql = "SELECT * FROM issues";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Issue issue = new Issue(
                    rs.getInt("id"),
                    rs.getInt("book_id"),
                    rs.getInt("user_id"),
                    rs.getDate("issue_date"),
                    rs.getDate("return_date"),
                    rs.getString("status")
                );
                issues.add(issue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return issues;
    }

    public Issue getIssueById(int id) {
        String sql = "SELECT * FROM issues WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Issue(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getInt("user_id"),
                        rs.getDate("issue_date"),
                        rs.getDate("return_date"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateIssue(Issue issue) {
        // If status is changed to 'returned', increment book's available count
        Issue oldIssue = getIssueById(issue.getId());
        boolean wasIssued = oldIssue != null && "issued".equalsIgnoreCase(oldIssue.getStatus());
        boolean isReturned = "returned".equalsIgnoreCase(issue.getStatus());
        String sql = "UPDATE issues SET book_id = ?, user_id = ?, issue_date = ?, return_date = ?, status = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, issue.getBookId());
            stmt.setInt(2, issue.getUserId());
            stmt.setDate(3, issue.getIssueDate());
            stmt.setDate(4, issue.getReturnDate());
            stmt.setString(5, issue.getStatus());
            stmt.setInt(6, issue.getId());
            stmt.executeUpdate();
            if (wasIssued && isReturned) {
                BookDAO bookDAO = new BookDAO();
                Book book = bookDAO.getBookById(issue.getBookId());
                if (book != null) {
                    book.setAvailable(book.getAvailable() + 1);
                    bookDAO.updateBook(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteIssue(int id) {
        String sql = "DELETE FROM issues WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 
