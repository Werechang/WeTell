package com.gebb.wetell.client;

/**
 * To easily test the gui and get an overview of methods used by the gui from the IGUICallable.
 */
public interface IGUICallable {
    void prepareClose();
    void onLoginPress(String username, String password);
}