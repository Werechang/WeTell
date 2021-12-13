package com.gebb.wetell.server;

import java.sql.*;

public class SQLManager {

    private Connection conn = null;

    protected SQLManager(String path) { //TODO path?
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

    protected void createDatabase() {
        try {
            String sql = "CREATE DATABASE wetell;";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Database successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void createTables() {
        // users
        try {
            String sql = "CREATE TABLE `users` (`id` int UNSIGNED NOT NULL AUTO_INCREMENT, `name` string, `hashedPassword` string, `salt` String, `profile_pic` string DEFAULT GETSPB(), PRIMARY KEY (id));";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("User table successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // messages
        try {
            String sql = "CREATE TABLE `messages` (`id` int unsigned NOT NULL AUTO_INCREMENT, `sender_id` int, `chat_id` int , `msg_content` string, `send_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (id), FOREIGN KEY (sender_id) REFERENCES users(id), FOREIGN KEY (chat_id) REFERENCES chats(id));";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Messages table successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // chats
        try {
            String sql = "CREATE TABLE `chats` (`id` int unsigned NOT NULL AUTO_INCREMENT, `profile_pic` string, `name` string, PRIMARY KEY (id));";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Chats table successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // contacts
        try {
            String sql = "CREATE TABLE `contacts` (`user_id` int , `chat_id` int , FOREIGN KEY (user_id) REFERENCES users(id), FOREIGN KEY (chat_id) REFERENCES chats(id));";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Contacts table successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void addUser(String username, String hashedPassword, String salt) {
        String sqlq = "SELECT username FROM users WHERE username = ?)";
        String sql = "INSERT INTO users(username,hashedPassword,salt) VALUES(?,?,?)";
        try {
            PreparedStatement pstmtq = conn.prepareStatement(sqlq);
            pstmtq.setString(1, username);
            ResultSet result = pstmtq.executeQuery();
            if(result.next()) {
                System.out.println("User already exists.");
            } else {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, salt);
                pstmt.executeUpdate();
                System.out.println("User successfully added.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void newMessage(int sender_id, int chat_id, String msg_content) {
        String sql = "INSERT INTO messages(sender_id,chat_id,msg_content) VALUES(?,?,?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sender_id);
            pstmt.setInt(2, chat_id);
            pstmt.setString(3, msg_content);
            pstmt.executeUpdate();
            System.out.println("Message successfully added.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected UserData getUser(String username, String hashedPassword) {
        String sql = "SELECT salt, hashedPassword FROM users WHERE username = ? AND hashedPassword = ?)";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet result = pstmt.executeQuery();
            if(result.next()) {
                return null; //TODO
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected void getMessagesForChat(){

    }

    public static class UserData {
        private String salt;
        private String hashedPassword;

        public UserData(String salt, String hashedPassword) {
            this.salt = salt;
            this.hashedPassword = hashedPassword;
        }

        public String getSalt() {
            return salt;
        }
        public String getHashedPassword() {
            return hashedPassword;
        }
    }
}
