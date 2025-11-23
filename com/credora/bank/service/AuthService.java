package com.credora.bank.service;

import com.credora.bank.dao.UserDAO;
import com.credora.bank.exceptions.InvalidCredentialsException;
import com.credora.bank.model.User;
import com.credora.bank.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService(Connection conn) {
        this.userDAO = new UserDAO(conn);
    }

    public User login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new InvalidCredentialsException("User not found");
        }
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid password");
        }
        return user;
    }

    public User register(String username, String rawPassword, String role) throws SQLException {
        String hash = PasswordUtil.hash(rawPassword);
        return userDAO.create(username, hash, role);
    }
}
