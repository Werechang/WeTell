package com.gebb.wetell.server;

import java.sql.*;

public class SQLManager {

    private Connection conn = null;

    protected SQLManager(String path) {
        try {
            // create a connection to the database
            conn = DriverManager.getConnection(path);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void createTables() {
        String sql = """
                CREATE TABLE `users` (`id` int PRIMARY KEY, `name` string, `hashedPassword` string, `salt` string, `profile_pic` string);\s
                CREATE TABLE `messages` ( `id` int PRIMARY KEY, `sender_id` int FOREIGN KEY, `chat_id` int FOREIGN KEY, `msg_content` string, `send_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);\s
                CREATE TABLE `chats` (`id` int PRIMARY KEY, `profile_pic` string, `name` string);\s
                CREATE TABLE `contacts` (`user_id` int FOREIGN KEY, `chat_id` int FOREIGN KEY);""";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void addUser(String username, String password) {
        String sql = "INSERT INTO users(username,password) VALUES(?,?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
