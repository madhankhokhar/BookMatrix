package com.library;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private boolean registered = false;

    public RegisterDialog(Frame parent) {
        super(parent, "Register", true);
        setLayout(new BorderLayout());
        setSize(350, 250);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> userTypeBox = new JComboBox<>(new String[]{"student", "staff"});
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("User Type:"));
        panel.add(userTypeBox);
        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton registerBtn = new JButton("Register");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String userType = (String) userTypeBox.getSelectedItem();
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            UserDAO userDAO = new UserDAO();
            if (userDAO.authenticateUser(email, password) != null) {
                JOptionPane.showMessageDialog(this, "User already exists with this email.");
                return;
            }
            userDAO.addUser(new User(0, name, email, userType, password, "pending"));
            registered = true;
            JOptionPane.showMessageDialog(this, "Registration submitted! Awaiting admin approval.");
            dispose();
        });

        cancelBtn.addActionListener(e -> {
            registered = false;
            dispose();
        });
    }

    public boolean isRegistered() {
        return registered;
    }
} 