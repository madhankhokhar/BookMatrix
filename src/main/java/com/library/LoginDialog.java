package com.library;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private User loggedInUser = null;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        setLayout(new BorderLayout());
        setSize(350, 200);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            UserDAO userDAO = new UserDAO();
            User user = userDAO.authenticateUser(email, password);
            if (user != null) {
                loggedInUser = user;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> {
            RegisterDialog registerDialog = new RegisterDialog((Frame) getParent());
            registerDialog.setVisible(true);
            if (registerDialog.isRegistered()) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.");
            }
        });

        cancelBtn.addActionListener(e -> {
            loggedInUser = null;
            dispose();
        });
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
} 