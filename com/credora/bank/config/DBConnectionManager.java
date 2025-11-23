package com.credora.bank.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionManager {

    private static final String URL = "jdbc:mysql://localhost:3306/credora_bank?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";       // change if needed
    private static final String PASSWORD = "your_password"; // change

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // load MySQL driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
