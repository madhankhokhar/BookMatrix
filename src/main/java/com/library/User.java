package com.library;

public class User {
    private int id;
    private String name;
    private String email;
    private String userType;
    private String password;
    private String status;

    public User(int id, String name, String email, String userType, String password, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.userType = userType;
        this.password = password;
        this.status = status;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 