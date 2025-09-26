package com.yourcompany.clientmanagement.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/client_management";
    private static final String USER = "root";
    private static final String PASSWORD = "tarek010203"; // or "root" if set

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

