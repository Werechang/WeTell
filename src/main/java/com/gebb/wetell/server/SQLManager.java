package com.gebb.wetell.server;

import java.sql.*;

public class SQLManager {

    private Connection conn = null;

    public SQLManager(String path) {
        try {
            // create a connection to the database
            conn = DriverManager.getConnection(path);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    protected void createUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username,password) VALUES(?,?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);
    }


    protected void createUsersTable() throws SQLException {
        String sql = "CREATE TABLE `users` (`id` int PRIMARY KEY, `name` string, `hashedPassword` string, `salt` string, `profile_pic` string);";

        PreparedStatement stmt = conn.prepareStatement(sql);
    }

    protected void createMessagesTable() throws SQLException {
        String sql = "CREATE TABLE `messages` ( `id` int PRIMARY KEY, `sender_id` int FOREIGN KEY, `chat_id` int FOREIGN KEY, `msg_content` string, `send_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";

        PreparedStatement stmt = conn.prepareStatement(sql);
    }

    protected void createChatsTable() throws SQLException {
        String sql = "CREATE TABLE `chats` (`id` int PRIMARY KEY, `profile_pic` string, `name` string);";

        PreparedStatement stmt = conn.prepareStatement(sql);
    }

    protected void createContactsTable() throws SQLException {
        String sql = "CREATE TABLE `contacts` (`user_id` int FOREIGN KEY, `chat_id` int FOREIGN KEY);";

        PreparedStatement stmt = conn.prepareStatement(sql);
    }
}
