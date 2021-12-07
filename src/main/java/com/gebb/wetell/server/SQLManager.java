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

    protected void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void createTables() {
        // users
        try {
            String sql = "CREATE TABLE `users` (`id` int UNSIGNED NOT NULL AUTO_INCREMENT, `name` string, `hashedPassword` string, `salt` String, `profile_pic` string DEFAULT GETSPB(), PRIMARY KEY (id));";
            PreparedStatement stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // messages
        try {
            String sql = "CREATE TABLE `messages` (`id` int unsigned NOT NULL AUTO_INCREMENT, `sender_id` int, `chat_id` int , `msg_content` string, `send_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (id), FOREIGN KEY (sender_id) REFERENCES users(id), FOREIGN KEY (chat_id) REFERENCES chats(id));";
            PreparedStatement stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // chats
        try {
            String sql = "CREATE TABLE `chats` (`id` int unsigned NOT NULL AUTO_INCREMENT, `profile_pic` string, `name` string, PRIMARY KEY (id));";
            PreparedStatement stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // contacts
        try {
            String sql = "CREATE TABLE `contacts` (`user_id` int , `chat_id` int , FOREIGN KEY (user_id) REFERENCES users(id), FOREIGN KEY (chat_id) REFERENCES chats(id));";
            PreparedStatement stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void createDatabase() {
        try {
            String sql = "CREATE DATABASE wetell;";
            PreparedStatement stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void addUser(String username, String hashedPassword, String salt) {
        String sql = "INSERT INTO users(username,hashedPassword,salt) VALUES(?,?,?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void newMessage(int sender_id, int chat_id, String msg_content){
        String sql = "INSERT INTO messages(sender_id,chat_id,msg_content) VALUES(?,?,?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, sender_id);
            stmt.setInt(2, chat_id);
            stmt.setString(3, msg_content);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
