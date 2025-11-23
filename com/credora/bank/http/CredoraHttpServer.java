package com.credora.bank.http;

import com.credora.bank.util.PasswordUtil;
import com.credora.bank.db.DBConnection;
import com.credora.bank.service.AccountService;
import com.credora.bank.service.TransactionService;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CredoraHttpServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        /* -----------------------
           /api/ping  (health)
        ------------------------ */
        server.createContext("/api/ping", exchange -> {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            sendJson(exchange, 200, "{\"status\":\"ok\"}");
        });

        /* ------------------------------------------------
           ACCOUNT OPENING (SIGNUP) + HASHED PASSWORD
           POST /api/accounts/open
        ------------------------------------------------ */
        server.createContext("/api/accounts/open", exchange -> {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseQuery(body);

            String fullName          = form.get("fullName");
            String email             = form.get("email");
            String password          = form.get("password");
            String phone             = form.get("phone");              // from frontend
            String initialDepositStr = form.get("initialDeposit");
            String address           = form.getOrDefault("address", ""); // optional

            if (fullName == null || email == null || password == null || initialDepositStr == null || phone == null) {
                sendJson(exchange, 400, "{\"error\":\"Missing required fields\"}");
                return;
            }

            BigDecimal initialDeposit;
            try {
                initialDeposit = new BigDecimal(initialDepositStr);
                if (initialDeposit.compareTo(new BigDecimal("100")) < 0) {
                    sendJson(exchange, 400, "{\"error\":\"Initial deposit must be at least 100\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, "{\"error\":\"Invalid initial deposit\"}");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                try {
                    conn.setAutoCommit(false);

                    // 1) Hash password
                    String passwordHash = PasswordUtil.hash(password);

                    // 2) Insert into customers
                    String sqlC = """
                            INSERT INTO customers(full_name, email, phone, address, password)
                            VALUES (?, ?, ?, ?, ?)
                            """;

                    var stmtC = conn.prepareStatement(sqlC, Statement.RETURN_GENERATED_KEYS);
                    stmtC.setString(1, fullName);
                    stmtC.setString(2, email);
                    stmtC.setString(3, phone);
                    stmtC.setString(4, address);
                    stmtC.setString(5, passwordHash);
                    stmtC.executeUpdate();

                    var rsC = stmtC.getGeneratedKeys();
                    if (!rsC.next()) {
                        conn.rollback();
                        sendJson(exchange, 500, "{\"error\":\"Failed to create customer\"}");
                        return;
                    }
                    long customerId = rsC.getLong(1);

                    // 3) Generate account number
                    String accountNumber = "CRD" + String.format("%05d", customerId);

                    // 4) Insert account
                    String sqlA = """
                            INSERT INTO accounts(account_number, customer_id, account_type, balance, status)
                            VALUES (?, ?, 'SAVINGS', ?, 'ACTIVE')
                            """;

                    var stmtA = conn.prepareStatement(sqlA);
                    stmtA.setString(1, accountNumber);
                    stmtA.setLong(2, customerId);
                    stmtA.setBigDecimal(3, initialDeposit);
                    stmtA.executeUpdate();

                    // 5) Initial transaction
                    String sqlT = """
                            INSERT INTO transactions(account_id, txn_type, amount, description, balance_after_txn)
                            VALUES (
                                (SELECT id FROM accounts WHERE account_number=?),
                                'DEPOSIT',
                                ?,
                                'Initial Deposit',
                                ?
                            )
                            """;

                    var stmtT = conn.prepareStatement(sqlT);
                    stmtT.setString(1, accountNumber);
                    stmtT.setBigDecimal(2, initialDeposit);
                    stmtT.setBigDecimal(3, initialDeposit);
                    stmtT.executeUpdate();

                    conn.commit();

                    String json = """
                            {
                              "message":"Account created",
                              "accountNumber":"%s"
                            }
                            """.formatted(accountNumber);

                    sendJson(exchange, 200, json);

                } catch (Exception e) {
                    conn.rollback();
                    e.printStackTrace();
                    sendJson(exchange, 500, "{\"error\":\"Server error\"}");
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Server error\"}");
            }
        });

        /* ---------------------------
           GET BALANCE
        ---------------------------- */
        server.createContext("/api/account/balance", exchange -> {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String accountNumber = params.get("accountNumber");

            if (accountNumber == null) {
                sendJson(exchange, 400, "{\"error\":\"accountNumber is required\"}");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                AccountService acc = new AccountService(conn);
                BigDecimal balance = acc.getAccountBalance(accountNumber);

                String json = String.format(
                        "{\"accountNumber\":\"%s\",\"balance\":%s}",
                        accountNumber,
                        balance.toPlainString()
                );
                sendJson(exchange, 200, json);

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Server error\"}");
            }
        });

        /* ---------------------------
           DEPOSIT
        ---------------------------- */
        server.createContext("/api/transaction/deposit", exchange -> {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseQuery(body);

            String accountNumber = form.get("accountNumber");
            String amountStr     = form.get("amount");

            if (accountNumber == null || amountStr == null) {
                sendJson(exchange, 400, "{\"error\":\"accountNumber and amount required\"}");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                TransactionService txn = new TransactionService(conn);
                BigDecimal amount = new BigDecimal(amountStr);

                txn.deposit(accountNumber, amount, "Web deposit");

                sendJson(exchange, 200, "{\"message\":\"Deposit successful\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Deposit failed\"}");
            }
        });

        /* ---------------------------
           WITHDRAW
        ---------------------------- */
        server.createContext("/api/transaction/withdraw", exchange -> {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseQuery(body);

            String accountNumber = form.get("accountNumber");
            String amountStr     = form.get("amount");

            if (accountNumber == null || amountStr == null) {
                sendJson(exchange, 400, "{\"error\":\"accountNumber and amount required\"}");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                TransactionService txn = new TransactionService(conn);
                BigDecimal amount = new BigDecimal(amountStr);

                try {
                    txn.withdraw(accountNumber, amount, "Web withdraw");
                    sendJson(exchange, 200, "{\"message\":\"Withdrawal successful\"}");
                } catch (IllegalArgumentException ex) {
                    if ("INSUFFICIENT_FUNDS".equals(ex.getMessage())) {
                        sendJson(exchange, 400, "{\"error\":\"Insufficient balance\"}");
                    } else {
                        sendJson(exchange, 400, "{\"error\":\"" + ex.getMessage() + "\"}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Withdrawal failed\"}");
            }
        });

        /* ---------------------------
           TRANSFER
        ---------------------------- */
        server.createContext("/api/transaction/transfer", exchange -> {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseQuery(body);

            String fromAcc   = form.get("fromAccountNumber");
            String toAcc     = form.get("toAccountNumber");
            String amountStr = form.get("amount");

            if (fromAcc == null || toAcc == null || amountStr == null) {
                sendJson(exchange, 400, "{\"error\":\"fromAccountNumber, toAccountNumber and amount required\"}");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                AccountService acc = new AccountService(conn);
                TransactionService txn = new TransactionService(conn);

                BigDecimal amount = new BigDecimal(amountStr);
                BigDecimal beforeFrom = acc.getAccountBalance(fromAcc);

                txn.transfer(fromAcc, toAcc, amount, "Web transfer");

                BigDecimal afterFrom = acc.getAccountBalance(fromAcc);

                String json = String.format(
                        "{\"fromAccount\":\"%s\",\"toAccount\":\"%s\",\"beforeFrom\":%s,\"afterFrom\":%s}",
                        fromAcc,
                        toAcc,
                        beforeFrom.toPlainString(),
                        afterFrom.toPlainString()
                );

                sendJson(exchange, 200, json);

            } catch (IllegalArgumentException ex) {
                sendJson(exchange, 400,
                        "{\"error\":\"" + ex.getMessage().replace("\"", "'") + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Server error\"}");
            }
        });

        /* ------------------------------------------------
           LOGIN  (HASHED PASSWORD CHECK)
           POST /api/auth/login
        ------------------------------------------------ */
        server.createContext("/api/auth/login", exchange -> {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> form = parseQuery(body);

            String email    = form.get("email");
            String password = form.get("password");

            if (email == null || password == null) {
                sendJson(exchange, 400, "{\"error\":\"Missing fields\"}");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                System.out.println("[LOGIN] email=" + email);

                // 1) Get user by email
                String sql = "SELECT id, password FROM customers WHERE email = ?";
                var stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                var rs = stmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("[LOGIN] No customer found for email");
                    sendJson(exchange, 401, "{\"error\":\"Invalid email or password\"}");
                    return;
                }

                String dbHash   = rs.getString("password");
                long customerId = rs.getLong("id");

                // 2) Compare USING PasswordUtil.verify
                if (!PasswordUtil.verify(password, dbHash)) {
                    System.out.println("[LOGIN] Password mismatch");
                    sendJson(exchange, 401, "{\"error\":\"Invalid email or password\"}");
                    return;
                }

                // 3) Fetch account number
                String sql2 = "SELECT account_number FROM accounts WHERE customer_id = ?";
                var stmt2 = conn.prepareStatement(sql2);
                stmt2.setLong(1, customerId);
                var rs2 = stmt2.executeQuery();

                if (!rs2.next()) {
                    System.out.println("[LOGIN] No account for customer");
                    sendJson(exchange, 500, "{\"error\":\"Account not found for customer\"}");
                    return;
                }

                String accountNumber = rs2.getString("account_number");
                String token = java.util.UUID.randomUUID().toString();

                String json = """
                        {
                          "message":"Login success",
                          "token":"%s",
                          "accountNumber":"%s"
                        }
                        """.formatted(token, accountNumber);

                sendJson(exchange, 200, json);

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Server error\"}");
            }
        });

        /* ------------------------------ */
        server.setExecutor(null);
        server.start();
        System.out.println("HTTP API running at http://localhost:8080");
    }

    /* ---------------------------------------------------------
       HELPERS
    ---------------------------------------------------------- */
    private static void addCorsHeaders(HttpExchange exchange) {
        var h = exchange.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJson(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> m = new HashMap<>();
        if (q == null || q.isBlank()) return m;
        for (String p : q.split("&")) {
            String[] kv = p.split("=", 2);
            m.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return m;
    }
}
