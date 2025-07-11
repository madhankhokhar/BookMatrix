package com.library;

public class Book {
    private int id;
    private String title;
    private String author;
    private String publisher;
    private int year;
    private String status;
    private int available;

    public Book(int id, String title, String author, String publisher, int year, String status, int available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.status = status;
        this.available = available;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
} 