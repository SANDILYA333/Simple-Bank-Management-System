package com.credora.bank.test;

import com.credora.bank.db.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            System.out.println("Connected to MySQL via DBConnection!");

            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                System.out.println("Users in table: " + rs.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
