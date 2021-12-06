package com.gebb.wetell.server;

import java.sql.*;

public class SQLManager {

    private static Connection conn;

    public SQLManager(String path, String username, String password) {
        conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:/db/wetell.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

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

    protected void createuser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username,password) VALUES(?,?)"; //TODO
        PreparedStatement stmt = conn.prepareStatement(sql);
    }


    protected static void createUsersTable() throws SQLException {

        String sql = "CREATE TABLE `users` (\n"

                + "`id` int PRIMARY KEY,\n"
                + "`name` string,\n"
                + "`hashedPassword` string,\n"
                + "`salt` string,\n"
                + "`profile_pic` string,\n"
                + ");";

        PreparedStatement stmt = conn.prepareStatement(sql);
    }

    protected static void createMessagesTable() throws SQLException {

        String sql = "CREATE TABLE `messages` (\n"

                + "`id` int PRIMARY KEY,\n"
                + "`sender_id` int FOREIGN KEY,\n"
                + "`chat_id` int FOREIGN KEY,\n"
                + "`msg_content` string,\n"
                + "`send_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n"
                + ");";

        PreparedStatement stmt = conn.prepareStatement(sql);
    }

    protected static void createChatsTable() throws SQLException {

        String sql = "CREATE TABLE `chats` (\n"

                + "`id` int PRIMARY KEY,\n"
                + "`profile_pic` string,\n"
                + "`name` string,\n"
                + ");";

        PreparedStatement stmt = conn.prepareStatement(sql);
    }

    protected static void createContactsTable() throws SQLException {

        String sql = "CREATE TABLE `contacts` (\n"

                + "`user_id` int FOREIGN KEY,\n"
                + "`chat_id` int FOREIGN KEY,\n"
                + ");";

        PreparedStatement stmt = conn.prepareStatement(sql);
    }


    public static void main(String[] args) {
        //createNewTable();
    }

}
