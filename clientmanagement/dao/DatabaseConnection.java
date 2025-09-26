package com.yourcompany.clientmanagement.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/client_management?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "tarek010203"; // or "root" if you set one

    // Static block to load driver once
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC driver loaded.");
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading MySQL JDBC Driver");
            e.printStackTrace();
        }
    }

    // Method to get a connection
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to MySQL database.");
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }

    // Test connection method
    public static boolean testConnection() {
        try {
            Connection connection = getConnection();
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection test successful.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}

