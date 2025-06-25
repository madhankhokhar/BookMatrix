package com.library;

import java.sql.Date;

public class Request {
    private int id;
    private int bookId;
    private int userId;
    private Date requestDate;
    private String status;
    private Date returnDate;

    public Request(int id, int bookId, int userId, Date requestDate, String status, Date returnDate) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.requestDate = requestDate;
        this.status = status;
        this.returnDate = returnDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Date getRequestDate() { return requestDate; }
    public void setRequestDate(Date requestDate) { this.requestDate = requestDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
} 