package com.gebb.wetell.client;

/**
 * To easily test the gui and get an overview of methods used by the gui from the IGUICallable in setOnAction().
 */
public interface IGUICallable {
    void prepareClose();
    void onLoginPress(String username, String password);
    void onSignInPress(String username, String password);
    void onLogoutPress();
    void onSelectChat(int chatId);
    void onSendMessage(String content);
    void onAddChat(String chatName);
    void onAddUserToChat(int chatId, int userId);
    void backtoMessagePane();
}
