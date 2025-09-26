package com.yourcompany.clientmanagement.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class ModernLoginPage extends JFrame {
    private final Color PRIMARY_COLOR = new Color(59, 89, 182);
    private final Color ACCENT_COLOR = new Color(76, 175, 80);

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;

    private HashMap<String, String> userDatabase;

    private boolean loginSuccessful = false;
    private String loggedInUser;

    public ModernLoginPage() {
        initializeUserDatabase();

        setTitle("Modern Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("LOGIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        JLabel usernameLabel = new JLabel("Username");
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(300, 40));

        JLabel passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(300, 40));

        loginButton = new JButton("Sign In");
        loginButton.setBackground(PRIMARY_COLOR);
        loginButton.setForeground(Color.WHITE);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);

        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        });

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(loginButton);
        formPanel.add(errorLabel);

        mainPanel.add(titleLabel);
        mainPanel.add(formPanel);
        add(mainPanel);
    }

    private void initializeUserDatabase() {
        userDatabase = new HashMap<>();
        userDatabase.put("admin", "admin123");
        userDatabase.put("user", "password");
        userDatabase.put("demo", "demo123");
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }

        if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
            System.out.println("Login successful for user: " + username);
            loginSuccessful = true;
            loggedInUser = username;
            
            // Clear the error label
            errorLabel.setText(" ");
            
            // Show success message briefly
            errorLabel.setForeground(new Color(46, 125, 50)); // Green color
            errorLabel.setText("Login successful! Loading application...");
            
            // Close the window after a brief delay to show the success message
            Timer timer = new Timer(500, e -> {
                setVisible(false);
                dispose(); // This will trigger the windowClosed event
            });
            timer.setRepeats(false);
            timer.start();
            
        } else {
            System.out.println("Login failed for user: " + username);
            errorLabel.setForeground(Color.RED);
            errorLabel.setText("Invalid username or password");
            
            // Clear password field on failed login
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    public String getLoggedInUser() {
        return loggedInUser;
    }
}
