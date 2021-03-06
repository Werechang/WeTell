package com.gebb.wetell.server;

import com.gebb.wetell.dataclasses.ChatData;
import com.gebb.wetell.dataclasses.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class SQLManager {

    private Connection conn = null;

    protected SQLManager(@NotNull String path) {
        try {
            // create a connection to the database
            conn = DriverManager.getConnection(path);
            System.out.println("Connected to database successfully");
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
                    "sent_at TEXT DEFAULT '2021-12-1 12:00:00.000', " +
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
                    "name TEXT," +
                    "salt TEXT)";
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
        // Chat secrets + salt
        try {
            String sql = "CREATE TABLE secrets (" +
                    "user_id INTEGER , " +
                    "chat_id INTEGER , " +
                    "user_chat_secret TEXT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE NO ACTION, " +
                    "FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE ON UPDATE NO ACTION);";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Secrets table successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected int addUser(@NotNull String username, @NotNull String hashedPassword, @NotNull String salt) {
        String sqlq = "SELECT id FROM users WHERE name = ?";
        String sql = "INSERT INTO users(name,hashedPassword,salt) VALUES(?,?,?)";
        try {
            PreparedStatement pstmtq = conn.prepareStatement(sqlq);
            pstmtq.setString(1, username);
            ResultSet result = pstmtq.executeQuery();
            if (result.next()) {
                throw new NullPointerException();
            } else {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, salt);
                pstmt.executeUpdate();
                PreparedStatement getId = conn.prepareStatement(sqlq);
                getId.setString(1, username);
                ResultSet set = getId.executeQuery();
                return set.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected synchronized int addChat(@NotNull String name, @NotNull String salt) {
        try {
            String sql = "INSERT INTO chats(name, salt) VALUES(?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, salt);
            statement.executeUpdate();

            String getId = "SELECT id FROM chats ORDER BY id DESC LIMIT 1";
            PreparedStatement preparedStatement = conn.prepareStatement(getId);
            return preparedStatement.executeQuery().getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected synchronized void addContact(@Range(from = 1, to = Integer.MAX_VALUE) int user_id, @Range(from = 1, to = Integer.MAX_VALUE) int chat_id) {
        String sqlq = "SELECT * FROM contacts WHERE user_id = ? AND chat_id = ?";
        String sql = "INSERT INTO contacts(user_id,chat_id) VALUES(?,?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sqlq);
            statement.setInt(1, user_id);
            statement.setInt(2, chat_id);
            ResultSet set = statement.executeQuery();
            if (set.next() && set.getInt("user_id") < 1) {
                throw new NullPointerException();
            }
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            pstmt.setInt(2, chat_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected synchronized String newMessage(@Range(from = 1, to = Integer.MAX_VALUE) int sender_id, @Range(from = 1, to = Integer.MAX_VALUE) int chat_id, @NotNull String msg_content) {
        String sqlq = "SELECT * FROM contacts WHERE chat_id = ? AND user_id = ?";
        String sql = "INSERT INTO messages(sender_id,chat_id,msg_content,sent_at) VALUES(?,?,?,(SELECT datetime('now', 'localtime')))";
        String getTime = "SELECT sent_at FROM messages ORDER BY id DESC LIMIT 1";
        try {
            // Check if the user is even in the chat
            PreparedStatement statement = conn.prepareStatement(sqlq);
            statement.setInt(1, chat_id);
            statement.setInt(2, sender_id);
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                throw new NullPointerException();
            }

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sender_id);
            pstmt.setInt(2, chat_id);
            pstmt.setString(3, msg_content);
            pstmt.executeUpdate();
            PreparedStatement preparedStatement = conn.prepareStatement(getTime);
            return preparedStatement.executeQuery().getString("sent_at");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected synchronized void overrideUserHash(@NotNull String username, @NotNull String hashedPassword, @NotNull String salt) {
        String sql = "UPDATE users SET hashedPassword = ?, salt = ? WHERE name = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, salt);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected int getUserId(@NotNull String username) {
        String sql = "SELECT id FROM users WHERE name = ?";
        try {
            PreparedStatement pstmtq = conn.prepareStatement(sql);
            pstmtq.setString(1, username);
            ResultSet result = pstmtq.executeQuery();
            if (result.next()) {
                return result.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected String getUsername(@Range(from = 1, to = Integer.MAX_VALUE) int userId) {
        String sql = "SELECT name FROM users WHERE id = ?";
        try {
            PreparedStatement pstmtq = conn.prepareStatement(sql);
            pstmtq.setInt(1, userId);
            ResultSet result = pstmtq.executeQuery();
            if (result.next()) {
                return result.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected synchronized UserData getUserData(@NotNull String username) {
        String sql = "SELECT salt, hashedPassword FROM users WHERE name = ?";
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

    protected ChatData getChatInfo(@Range(from = 1, to = Integer.MAX_VALUE) int chatId) {
        String sql = "SELECT name, salt FROM chats WHERE id = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, chatId);
            ResultSet result = pstmt.executeQuery();
            if (result.next()) {
                return new ChatData(result.getString("name"), chatId, result.getString("salt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected ArrayList<Integer> getUsersInChat(@Range(from = 1, to = Integer.MAX_VALUE) int chatId) {
        String sql = "SELECT user_id FROM contacts WHERE chat_id = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, chatId);
            ResultSet result = pstmt.executeQuery();
            ArrayList<Integer> temp = new ArrayList<>(2);
            while (result.next()) {
                temp.add(result.getInt("user_id"));
            }
            if (temp.size() != 0) {
                return temp;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected ArrayList<MessageData> fetchMessagesForChat(@Range(from = 1, to = Integer.MAX_VALUE) int chat_id, @Range(from = 1, to = Integer.MAX_VALUE) int user_id) {
        String sqlq = "SELECT chat_id FROM contacts WHERE user_id = ? AND chat_id = ?";
        String sql = "SELECT sender_id, msg_content, sent_at FROM messages WHERE chat_id = ? ORDER BY id DESC LIMIT 20";
        try {
            PreparedStatement statement = conn.prepareStatement(sqlq);
            statement.setInt(1, user_id);
            statement.setInt(2, chat_id);
            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                throw new NullPointerException();
            }
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, chat_id);
            ResultSet result = pstmt.executeQuery();
            ArrayList<MessageData> temp = new ArrayList<>(20);
            while (result.next()) {
                temp.add(new MessageData(result.getInt("sender_id"), chat_id, result.getString("msg_content"), result.getString("sent_at")));
            }
            // Reverse to send the newest at the bottom
            Collections.reverse(temp);
            return temp;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    protected ArrayList<ChatData> fetchChatsForUser(@Range(from = 1, to = Integer.MAX_VALUE) int user_id) {
        String sql = "SELECT contacts.chat_id, chats.name, chats.salt FROM contacts " +
                "LEFT JOIN chats on contacts.chat_id = chats.id " +
                "WHERE contacts.user_id = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user_id);
            ResultSet result = pstmt.executeQuery();
            ArrayList<ChatData> temp = new ArrayList<>(20);
            while (result.next()) {
                temp.add(new ChatData(result.getString("name"), result.getInt("chat_id"), result.getString("salt")));
            }
            return temp;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
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
