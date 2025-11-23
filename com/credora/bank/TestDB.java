package com.credora.bank;

import com.credora.bank.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected to MySQL!");

            String sql = "SELECT COUNT(*) AS cnt FROM users";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    System.out.println("Users in table: " + count);
                }
            }
        } catch (Exception e) {
            System.out.println(" Connection failed");
            e.printStackTrace();
        }
    }
}
