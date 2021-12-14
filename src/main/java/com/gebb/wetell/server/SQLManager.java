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
        // delete tables
        try {
            Statement statement = conn.createStatement();
            statement.execute("DROP TABLE users");
            statement.execute("DROP TABLE messages");
            statement.execute("DROP TABLE chats");
            statement.execute("DROP TABLE contacts");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            String sql = "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY ASC, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "hashedPassword TEXT NOT NULL, " +
                    "salt TEXT NOT NULL, " +
                    "profile_pic TEXT DEFAULT NULL)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("User table successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // messages
        try {
            String sql = "CREATE TABLE messages (" +
                    "id INTEGER PRIMARY KEY ASC, " +
                    "sender_id INTEGER, " +
                    "chat_id INTEGER , " +
                    "msg_content TEXT NOT NULL, " +
                    "send_at TEXT DEFAULT '2021-12-1 12:00:00.000', " +
                    "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE NO ACTION, " +
                    "FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE ON UPDATE NO ACTION)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Messages table successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // chats
        try {
            String sql = "CREATE TABLE chats (" +
                    "id INTEGER PRIMARY KEY ASC, " +
                    "profile_pic TEXT, " +
                    "name TEXT);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Chats table successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // contacts
        try {
            String sql = "CREATE TABLE contacts (" +
                    "user_id INTEGER , " +
                    "chat_id INTEGER , " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE NO ACTION, " +
                    "FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE ON UPDATE NO ACTION);";
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
            if (result.next()) {
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

    protected UserData getUser(String username) {
        String sql = "SELECT salt, hashedPassword FROM users WHERE username = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                return new UserData(result.getString("salt"), result.getString("hashedPassword"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected void getMessagesForChat() {

    }

    public static class UserData {
        private final String salt;
        private final String hashedPassword;

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
