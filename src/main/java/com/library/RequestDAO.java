package com.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root";
    private static final String PASSWORD = "Sql@123#";

    public void addRequest(Request request) {
        String sql = "INSERT INTO requests (book_id, user_id, request_date, status, return_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getBookId());
            stmt.setInt(2, request.getUserId());
            stmt.setDate(3, request.getRequestDate());
            stmt.setString(4, request.getStatus());
            stmt.setDate(5, request.getReturnDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Request> getRequestsByUserId(int userId) {
        List<Request> requests = new ArrayList<>();
        String sql = "SELECT * FROM requests WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new Request(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getInt("user_id"),
                        rs.getDate("request_date"),
                        rs.getString("status"),
                        rs.getDate("return_date")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public List<Request> getAllRequests() {
        List<Request> requests = new ArrayList<>();
        String sql = "SELECT * FROM requests";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(new Request(
                    rs.getInt("id"),
                    rs.getInt("book_id"),
                    rs.getInt("user_id"),
                    rs.getDate("request_date"),
                    rs.getString("status"),
                    rs.getDate("return_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public void updateRequest(Request request) {
        String sql = "UPDATE requests SET book_id = ?, user_id = ?, request_date = ?, status = ?, return_date = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, request.getBookId());
            stmt.setInt(2, request.getUserId());
            stmt.setDate(3, request.getRequestDate());
            stmt.setString(4, request.getStatus());
            stmt.setDate(5, request.getReturnDate());
            stmt.setInt(6, request.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Request getRequestById(int id) {
        String sql = "SELECT * FROM requests WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Request(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getInt("user_id"),
                        rs.getDate("request_date"),
                        rs.getString("status"),
                        rs.getDate("return_date")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
} 