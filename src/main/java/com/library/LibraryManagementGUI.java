package com.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class LibraryManagementGUI extends JFrame {
    private BookDAO bookDAO = new BookDAO();
    private DefaultTableModel bookTableModel;
    private JTable bookTable;
    private UserDAO userDAO = new UserDAO();
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private IssueDAO issueDAO = new IssueDAO();
    private DefaultTableModel issueTableModel;
    private JTable issueTable;
    private User loggedInUser;

    public LibraryManagementGUI(User user) {
        this.loggedInUser = user;
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Welcome message and Logout button
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getName() + " (" + user.getUserType() + ")");
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);
                User newUser = loginDialog.getLoggedInUser();
                if (newUser != null) {
                    new LibraryManagementGUI(newUser).setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        });
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        // Student: Welcome, Books, My Requests, Issues. Others: Books, Users, Requests, Issues
        if ("student".equalsIgnoreCase(user.getUserType())) {
            tabbedPane.addTab("Welcome", createStudentWelcomePanel());
            tabbedPane.addTab("Books", createBooksPanel());
            tabbedPane.addTab("My Requests", createStudentRequestsPanel());
            tabbedPane.addTab("Issues", createIssuesPanel());
        } else {
            tabbedPane.addTab("Books", createBooksPanel());
            if (!"student".equalsIgnoreCase(user.getUserType())) {
                tabbedPane.addTab("Users", createUsersPanel());
            }
            tabbedPane.addTab("Requests", createStaffRequestsPanel());
            tabbedPane.addTab("Issues", createIssuesPanel());
            if ("admin".equalsIgnoreCase(user.getUserType())) {
                tabbedPane.addTab("Approvals", createApprovalsPanel());
            }
        }
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        bookTableModel = new DefaultTableModel(new Object[]{"ID", "Title", "Author", "Publisher", "Year", "Status", "Available"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        bookTable = new JTable(bookTableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(bookTableModel);
        bookTable.setRowSorter(sorter);
        refreshBookTable();

        // Search field
        JPanel searchPanel = new JPanel(new BorderLayout());
        JLabel searchLabel = new JLabel("Search: ");
        JTextField searchField = new JTextField();
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Only show action buttons for non-students
        if (!"student".equalsIgnoreCase(loggedInUser.getUserType())) {
            JPanel buttonPanel = new JPanel();
            JButton addBtn = new JButton("Add");
            JButton updateBtn = new JButton("Update");
            JButton deleteBtn = new JButton("Delete");
            JButton refreshBtn = new JButton("Refresh");
            buttonPanel.add(addBtn);
            buttonPanel.add(updateBtn);
            buttonPanel.add(deleteBtn);
            buttonPanel.add(refreshBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            // Only admin can delete, only admin/staff can add/update
            if (!"admin".equalsIgnoreCase(loggedInUser.getUserType())) {
                deleteBtn.setEnabled(false);
            }
            // Add/Update logic for staff already handled above
            addBtn.addActionListener((ActionEvent e) -> {
                Book book = showBookDialog(null);
                if (book != null) {
                    bookDAO.addBook(book);
                    refreshAllTabs();
                }
            });
            updateBtn.addActionListener((ActionEvent e) -> {
                int row = bookTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select a book to update.");
                    return;
                }
                int id = (int) bookTableModel.getValueAt(row, 0);
                Book oldBook = bookDAO.getBookById(id);
                Book updatedBook = showBookDialog(oldBook);
                if (updatedBook != null) {
                    updatedBook.setId(id);
                    bookDAO.updateBook(updatedBook);
                    refreshAllTabs();
                }
            });
            deleteBtn.addActionListener((ActionEvent e) -> {
                int row = bookTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select a book to delete.");
                    return;
                }
                int id = (int) bookTableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        bookDAO.deleteBook(id);
                        refreshAllTabs();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Failed to delete book: " + ex.getMessage());
                    }
                }
            });
            refreshBtn.addActionListener(e -> refreshBookTable());
        } else {
            // For students, add a Request Issue button
            JPanel buttonPanel = new JPanel();
            JButton requestBtn = new JButton("Request Issue");
            JButton refreshBtn = new JButton("Refresh");
            buttonPanel.add(requestBtn);
            buttonPanel.add(refreshBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            requestBtn.addActionListener((ActionEvent e) -> {
                int row = bookTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select a book to request.");
                    return;
                }
                int bookId = (int) bookTableModel.getValueAt(row, 0);
                int available = (int) bookTableModel.getValueAt(row, 6);
                if (available <= 0) {
                    JOptionPane.showMessageDialog(this, "This book is not currently available.");
                    return;
                }
                // Check if already requested or issued
                RequestDAO requestDAO = new RequestDAO();
                java.util.List<Request> myRequests = requestDAO.getRequestsByUserId(loggedInUser.getId());
                for (Request r : myRequests) {
                    if (r.getBookId() == bookId && ("pending".equalsIgnoreCase(r.getStatus()) || "approved".equalsIgnoreCase(r.getStatus()))) {
                        JOptionPane.showMessageDialog(this, "You have already requested or been issued this book.");
                        return;
                    }
                }
                IssueDAO issueDAO = new IssueDAO();
                java.util.List<Issue> myIssues = issueDAO.getAllIssues();
                for (Issue i : myIssues) {
                    if (i.getBookId() == bookId && i.getUserId() == loggedInUser.getId() && "issued".equalsIgnoreCase(i.getStatus())) {
                        JOptionPane.showMessageDialog(this, "You have already been issued this book.");
                        return;
                    }
                }
                // Prompt for return date
                String returnDateStr = JOptionPane.showInputDialog(this, "Enter desired return date (yyyy-mm-dd):");
                if (returnDateStr == null || returnDateStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Return date is required.");
                    return;
                }
                java.sql.Date returnDate;
                try {
                    returnDate = java.sql.Date.valueOf(returnDateStr.trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-mm-dd.");
                    return;
                }
                // Add request with return date
                Request request = new Request(0, bookId, loggedInUser.getId(), new java.sql.Date(System.currentTimeMillis()), "pending", returnDate);
                requestDAO.addRequest(request);
                JOptionPane.showMessageDialog(this, "Request submitted!");
                refreshAllTabs();
            });
            refreshBtn.addActionListener(e -> refreshBookTable());
        }
        return panel;
    }

    private void refreshBookTable() {
        bookTableModel.setRowCount(0);
        List<Book> books = bookDAO.getAllBooks();
        for (Book b : books) {
            bookTableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getPublisher(), b.getYear(), b.getStatus(), b.getAvailable()});
        }
    }

    private Book showBookDialog(Book book) {
        JTextField titleField = new JTextField(book != null ? book.getTitle() : "");
        JTextField authorField = new JTextField(book != null ? book.getAuthor() : "");
        JTextField publisherField = new JTextField(book != null ? book.getPublisher() : "");
        JTextField yearField = new JTextField(book != null ? String.valueOf(book.getYear()) : "");
        JTextField statusField = new JTextField(book != null ? book.getStatus() : "available");
        JTextField availableField = new JTextField(book != null ? String.valueOf(book.getAvailable()) : "1");
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Title:")); panel.add(titleField);
        panel.add(new JLabel("Author:")); panel.add(authorField);
        panel.add(new JLabel("Publisher:")); panel.add(publisherField);
        panel.add(new JLabel("Year:")); panel.add(yearField);
        panel.add(new JLabel("Status:")); panel.add(statusField);
        panel.add(new JLabel("Available:")); panel.add(availableField);
        int result = JOptionPane.showConfirmDialog(this, panel, book == null ? "Add Book" : "Update Book", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String publisher = publisherField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());
                String status = statusField.getText().trim();
                int available = Integer.parseInt(availableField.getText().trim());
                return new Book(0, title, author, publisher, year, status, available);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
        return null;
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        userTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email", "User Type"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        userTable = new JTable(userTableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(userTableModel);
        userTable.setRowSorter(sorter);
        refreshUserTable();

        // Search field
        JPanel searchPanel = new JPanel(new BorderLayout());
        JLabel searchLabel = new JLabel("Search: ");
        JTextField searchField = new JTextField();
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Only admin can delete, only admin can add/update
        if (!"admin".equalsIgnoreCase(loggedInUser.getUserType())) {
            deleteBtn.setEnabled(false);
            addBtn.setEnabled(false);
            updateBtn.setEnabled(false);
        }

        addBtn.addActionListener((ActionEvent e) -> {
            User user = showUserDialog(null);
            if (user != null) {
                userDAO.addUser(user);
                refreshAllTabs();
            }
        });

        updateBtn.addActionListener((ActionEvent e) -> {
            int row = userTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a user to update.");
                return;
            }
            int id = (int) userTableModel.getValueAt(row, 0);
            User oldUser = userDAO.getUserById(id);
            User updatedUser = showUserDialog(oldUser);
            if (updatedUser != null) {
                updatedUser.setId(id);
                userDAO.updateUser(updatedUser);
                refreshAllTabs();
            }
        });

        deleteBtn.addActionListener((ActionEvent e) -> {
            int row = userTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a user to delete.");
                return;
            }
            int id = (int) userTableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    userDAO.deleteUser(id);
                    refreshAllTabs();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to delete user: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    private void refreshUserTable() {
        userTableModel.setRowCount(0);
        java.util.List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            userTableModel.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(), u.getUserType()});
        }
    }

    private User showUserDialog(User user) {
        JTextField nameField = new JTextField(user != null ? user.getName() : "");
        JTextField emailField = new JTextField(user != null ? user.getEmail() : "");
        JTextField userTypeField = new JTextField(user != null ? user.getUserType() : "");
        JPasswordField passwordField = new JPasswordField(user != null ? user.getPassword() : "password");
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:")); panel.add(nameField);
        panel.add(new JLabel("Email:")); panel.add(emailField);
        panel.add(new JLabel("User Type:")); panel.add(userTypeField);
        panel.add(new JLabel("Password:")); panel.add(passwordField);
        int result = JOptionPane.showConfirmDialog(this, panel, user == null ? "Add User" : "Update User", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String userType = userTypeField.getText().trim();
                String password = new String(passwordField.getPassword());
                return new User(0, name, email, userType, password, "approved");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
        return null;
    }

    private JPanel createIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        issueTableModel = new DefaultTableModel(new Object[]{"ID", "Book ID", "User ID", "Issue Date", "Return Date", "Status"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        issueTable = new JTable(issueTableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(issueTableModel);
        issueTable.setRowSorter(sorter);
        refreshIssueTable();

        // Search field
        JPanel searchPanel = new JPanel(new BorderLayout());
        JLabel searchLabel = new JLabel("Search: ");
        JTextField searchField = new JTextField();
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        panel.add(new JScrollPane(issueTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshIssueTable());
        buttonPanel.add(refreshBtn);
        // Only show Add/Update/Delete for non-students
        if (!"student".equalsIgnoreCase(loggedInUser.getUserType())) {
            JButton addBtn = new JButton("Add");
            JButton updateBtn = new JButton("Update");
            JButton deleteBtn = new JButton("Delete");
            buttonPanel.add(addBtn);
            buttonPanel.add(updateBtn);
            buttonPanel.add(deleteBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            if (!"admin".equalsIgnoreCase(loggedInUser.getUserType())) {
                deleteBtn.setEnabled(false);
            }
            addBtn.addActionListener((ActionEvent e) -> {
                Issue issue = showIssueDialog(null);
                if (issue != null) {
                    try {
                        issueDAO.addIssue(issue);
                        refreshIssueTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                    }
                }
            });
            updateBtn.addActionListener((ActionEvent e) -> {
                int row = issueTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select an issue to update.");
                    return;
                }
                int id = (int) issueTableModel.getValueAt(row, 0);
                Issue oldIssue = issueDAO.getIssueById(id);
                Issue updatedIssue = showIssueDialog(oldIssue);
                if (updatedIssue != null) {
                    updatedIssue.setId(id);
                    issueDAO.updateIssue(updatedIssue);
                    refreshIssueTable();
                }
            });
            deleteBtn.addActionListener((ActionEvent e) -> {
                int row = issueTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select an issue to delete.");
                    return;
                }
                int id = (int) issueTableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this issue?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        issueDAO.deleteIssue(id);
                        refreshIssueTable();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Failed to delete issue: " + ex.getMessage());
                    }
                }
            });
        } else {
            // For students, add a Return button for their issued books
            JButton returnBtn = new JButton("Return Selected Book");
            buttonPanel.add(returnBtn);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            returnBtn.addActionListener((ActionEvent e) -> {
                int row = issueTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select an issued book to return.");
                    return;
                }
                int id = (int) issueTableModel.getValueAt(row, 0);
                String status = (String) issueTableModel.getValueAt(row, 5);
                if (!"issued".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "This book is not currently issued.");
                    return;
                }
                Issue issue = issueDAO.getIssueById(id);
                if (issue != null) {
                    issue.setStatus("returned");
                    issueDAO.updateIssue(issue);
                    refreshIssueTable();
                    JOptionPane.showMessageDialog(this, "Book returned successfully.");
                    // Auto-refresh Issues panel
                    refreshIssueTable();
                }
            });
        }
        return panel;
    }

    private void refreshIssueTable() {
        issueTableModel.setRowCount(0);
        java.util.List<Issue> issues = issueDAO.getAllIssues();
        // For students, only show their own issues
        if ("student".equalsIgnoreCase(loggedInUser.getUserType())) {
            int studentId = loggedInUser.getId();
            for (Issue i : issues) {
                if (i.getUserId() == studentId) {
                    issueTableModel.addRow(new Object[]{i.getId(), i.getBookId(), i.getUserId(), i.getIssueDate(), i.getReturnDate(), i.getStatus()});
                }
            }
        } else {
            for (Issue i : issues) {
                issueTableModel.addRow(new Object[]{i.getId(), i.getBookId(), i.getUserId(), i.getIssueDate(), i.getReturnDate(), i.getStatus()});
            }
        }
    }

    private Issue showIssueDialog(Issue issue) {
        JTextField bookIdField = new JTextField(issue != null ? String.valueOf(issue.getBookId()) : "");
        JTextField userIdField = new JTextField(issue != null ? String.valueOf(issue.getUserId()) : "");
        JTextField issueDateField = new JTextField(issue != null ? String.valueOf(issue.getIssueDate()) : "");
        JTextField returnDateField = new JTextField(issue != null ? String.valueOf(issue.getReturnDate()) : "");
        JTextField statusField = new JTextField(issue != null ? issue.getStatus() : "issued");
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Book ID:")); panel.add(bookIdField);
        panel.add(new JLabel("User ID:")); panel.add(userIdField);
        panel.add(new JLabel("Issue Date (yyyy-mm-dd):")); panel.add(issueDateField);
        panel.add(new JLabel("Return Date (yyyy-mm-dd):")); panel.add(returnDateField);
        panel.add(new JLabel("Status:")); panel.add(statusField);
        int result = JOptionPane.showConfirmDialog(this, panel, issue == null ? "Add Issue" : "Update Issue", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int bookId = Integer.parseInt(bookIdField.getText().trim());
                int userId = Integer.parseInt(userIdField.getText().trim());
                java.sql.Date issueDate = java.sql.Date.valueOf(issueDateField.getText().trim());
                java.sql.Date returnDate = java.sql.Date.valueOf(returnDateField.getText().trim());
                String status = statusField.getText().trim();
                return new Issue(0, bookId, userId, issueDate, returnDate, status);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        }
        return null;
    }

    private JPanel createStudentWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        int issuedCount = 0;
        java.util.List<Issue> issues = issueDAO.getAllIssues();
        for (Issue i : issues) {
            if (i.getUserId() == loggedInUser.getId() && "issued".equalsIgnoreCase(i.getStatus())) {
                issuedCount++;
            }
        }
        JLabel welcomeMsg = new JLabel("Welcome, " + loggedInUser.getName() + "! You have " + issuedCount + " book(s) currently issued.");
        welcomeMsg.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeMsg.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(welcomeMsg, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStudentRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel requestTableModel = new DefaultTableModel(new Object[]{"ID", "Book ID", "Request Date", "Return Date", "Status"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable requestTable = new JTable(requestTableModel);
        RequestDAO requestDAO = new RequestDAO();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            requestTableModel.setRowCount(0);
            java.util.List<Request> myRequests = requestDAO.getRequestsByUserId(loggedInUser.getId());
            for (Request r : myRequests) {
                requestTableModel.addRow(new Object[]{r.getId(), r.getBookId(), r.getRequestDate(), r.getReturnDate(), r.getStatus()});
            }
        });
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        java.util.List<Request> myRequests = requestDAO.getRequestsByUserId(loggedInUser.getId());
        for (Request r : myRequests) {
            requestTableModel.addRow(new Object[]{r.getId(), r.getBookId(), r.getRequestDate(), r.getReturnDate(), r.getStatus()});
        }
        panel.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStaffRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel requestTableModel = new DefaultTableModel(new Object[]{"ID", "Book ID", "User ID", "Request Date", "Status"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable requestTable = new JTable(requestTableModel);
        RequestDAO requestDAO = new RequestDAO();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            requestTableModel.setRowCount(0);
            java.util.List<Request> allRequests = requestDAO.getAllRequests();
            for (Request r : allRequests) {
                requestTableModel.addRow(new Object[]{r.getId(), r.getBookId(), r.getUserId(), r.getRequestDate(), r.getStatus()});
            }
        });
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        java.util.List<Request> allRequests = requestDAO.getAllRequests();
        for (Request r : allRequests) {
            requestTableModel.addRow(new Object[]{r.getId(), r.getBookId(), r.getUserId(), r.getRequestDate(), r.getStatus()});
        }
        panel.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        approveBtn.addActionListener((ActionEvent e) -> {
            int row = requestTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a request to approve.");
                return;
            }
            int requestId = (int) requestTableModel.getValueAt(row, 0);
            String status = (String) requestTableModel.getValueAt(row, 4);
            if (!"pending".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This request is not pending.");
                return;
            }
            RequestDAO reqDAO = new RequestDAO();
            Request req = reqDAO.getRequestById(requestId);
            if (req != null) {
                // Issue the book
                BookDAO bookDAO = new BookDAO();
                Book book = bookDAO.getBookById(req.getBookId());
                if (book != null && book.getAvailable() > 0) {
                    IssueDAO issueDAO = new IssueDAO();
                    Issue issue = new Issue(0, req.getBookId(), req.getUserId(), new java.sql.Date(System.currentTimeMillis()), req.getReturnDate(), "issued");
                    try {
                        issueDAO.addIssue(issue);
                        req.setStatus("approved");
                        reqDAO.updateRequest(req);
                        JOptionPane.showMessageDialog(this, "Request approved and book issued.");
                        requestTableModel.setValueAt("approved", row, 4);
                        refreshAllTabs();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Book is not available.");
                }
            }
        });

        rejectBtn.addActionListener((ActionEvent e) -> {
            int row = requestTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a request to reject.");
                return;
            }
            int requestId = (int) requestTableModel.getValueAt(row, 0);
            String status = (String) requestTableModel.getValueAt(row, 4);
            if (!"pending".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "This request is not pending.");
                return;
            }
            RequestDAO reqDAO = new RequestDAO();
            Request req = reqDAO.getRequestById(requestId);
            if (req != null) {
                req.setStatus("rejected");
                reqDAO.updateRequest(req);
                JOptionPane.showMessageDialog(this, "Request rejected.");
                requestTableModel.setValueAt("rejected", row, 4);
                refreshAllTabs();
            }
        });

        return panel;
    }

    private JPanel createApprovalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel approvalsTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email", "User Type"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable approvalsTable = new JTable(approvalsTableModel);
        java.util.List<User> pendingUsers = userDAO.getAllUsers();
        for (User u : pendingUsers) {
            if ("pending".equalsIgnoreCase(u.getStatus())) {
                approvalsTableModel.addRow(new Object[]{u.getId(), u.getName(), u.getEmail(), u.getUserType()});
            }
        }
        panel.add(new JScrollPane(approvalsTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        approveBtn.addActionListener(e -> {
            int row = approvalsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a user to approve.");
                return;
            }
            int userId = (int) approvalsTableModel.getValueAt(row, 0);
            User user = userDAO.getUserById(userId);
            if (user != null) {
                user.setStatus("approved");
                userDAO.updateUser(user);
                approvalsTableModel.removeRow(row);
                JOptionPane.showMessageDialog(this, "User approved.");
                refreshAllTabs();
            }
        });
        rejectBtn.addActionListener(e -> {
            int row = approvalsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a user to reject.");
                return;
            }
            int userId = (int) approvalsTableModel.getValueAt(row, 0);
            User user = userDAO.getUserById(userId);
            if (user != null) {
                user.setStatus("rejected");
                userDAO.updateUser(user);
                approvalsTableModel.removeRow(row);
                JOptionPane.showMessageDialog(this, "User rejected.");
                refreshAllTabs();
            }
        });
        return panel;
    }

    // Add helper methods for auto-refresh
    private void refreshStudentRequestsPanel() {
        // Find the JTabbedPane
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) comp;
                int idx = tabbedPane.indexOfTab("My Requests");
                if (idx != -1) {
                    Component tab = tabbedPane.getComponentAt(idx);
                    JTable table = findTableInPanel(tab);
                    if (table != null && table.getModel() instanceof DefaultTableModel) {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        model.setRowCount(0);
                        RequestDAO requestDAO = new RequestDAO();
                        java.util.List<Request> myRequests = requestDAO.getRequestsByUserId(loggedInUser.getId());
                        for (Request r : myRequests) {
                            model.addRow(new Object[]{r.getId(), r.getBookId(), r.getRequestDate(), r.getReturnDate(), r.getStatus()});
                        }
                    }
                }
            }
        }
    }
    private void refreshStaffRequestsPanel() {
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) comp;
                int idx = tabbedPane.indexOfTab("Requests");
                if (idx != -1) {
                    Component tab = tabbedPane.getComponentAt(idx);
                    JTable table = findTableInPanel(tab);
                    if (table != null && table.getModel() instanceof DefaultTableModel) {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        model.setRowCount(0);
                        RequestDAO requestDAO = new RequestDAO();
                        java.util.List<Request> allRequests = requestDAO.getAllRequests();
                        for (Request r : allRequests) {
                            model.addRow(new Object[]{r.getId(), r.getBookId(), r.getUserId(), r.getRequestDate(), r.getStatus()});
                        }
                    }
                }
            }
        }
    }
    // Utility to find JTable in a panel
    private JTable findTableInPanel(Component panel) {
        if (panel instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) panel;
            JViewport viewport = scroll.getViewport();
            Component view = viewport.getView();
            if (view instanceof JTable) return (JTable) view;
        } else if (panel instanceof JPanel) {
            for (Component c : ((JPanel) panel).getComponents()) {
                JTable t = findTableInPanel(c);
                if (t != null) return t;
            }
        }
        return null;
    }

    private void refreshAllTabs() {
        refreshBookTable();
        refreshIssueTable();
        refreshUserTable();
        refreshStudentRequestsPanel();
        refreshStaffRequestsPanel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);
            User user = loginDialog.getLoggedInUser();
            if (user != null) {
                new LibraryManagementGUI(user).setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
} 