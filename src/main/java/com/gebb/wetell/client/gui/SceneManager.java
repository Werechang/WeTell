package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SceneManager {
    
    private final LoginPane loginPane;
    private final SignInPane signInPane;
    private final MessagePane messagePane;
    private final Scene scene;
    private final boolean hasResources;

    public SceneManager(Stage stage, IGUICallable callable, boolean hasResources) {
        this.hasResources = hasResources;

        stage.setOnCloseRequest(event -> callable.prepareClose());
        stage.setHeight(700);
        stage.setWidth(1000);
        stage.setMinHeight(300);
        stage.setMinWidth(400);

        this.loginPane = new LoginPane(callable, this);
        this.signInPane = new SignInPane(callable, this);
        this.messagePane = new MessagePane(callable, stage);

        // Login is the first scene
        this.scene = new Scene(this.loginPane);
        if (hasResources) {
            scene.getStylesheets().add(Objects.requireNonNull(LoginPane.class.getResource("stylesheets/login-dark.css")).toExternalForm());
        }
        stage.setScene(scene);
        stage.show();
    }
    
    public void setScene(SceneType type) {
        switch (type) {
            case LOGIN -> Platform.runLater(() -> {
                if (hasResources) {
                scene.getStylesheets().set(0, Objects.requireNonNull(SignInPane.class.getResource("stylesheets/login-dark.css")).toExternalForm());
                }
                scene.setRoot(loginPane);
            });
            case SIGNIN -> Platform.runLater(() -> {
                if (hasResources) {
                    scene.getStylesheets().set(0, Objects.requireNonNull(SignInPane.class.getResource("stylesheets/login-dark.css")).toExternalForm());
                }
                scene.setRoot(signInPane);
            });
            case MESSAGE -> Platform.runLater(() -> {
                if (hasResources) {
                    scene.getStylesheets().set(0, Objects.requireNonNull(SignInPane.class.getResource("stylesheets/login-dark.css")).toExternalForm());
                }
                scene.setRoot(messagePane);
            });
            default -> System.err.println("Scene not supported or set");
        }
    }

    public void setDisconnected(boolean value) {
        Platform.runLater(() -> signInPane.setDisconnectedLabel(value));
        Platform.runLater(() -> loginPane.setDisconnectedLabel(value));
    }

    public void setCurrentUserInformation(String username) {
    }

    /**
     * Add contact field to the contacts of this user
     * @param name
     */
    public void addChatInformation(String name, int id) {
        Platform.runLater(() -> messagePane.addChat(name, id));
    }

    public void addMessage(String messageContent, boolean isSentByThisUser) {
        Platform.runLater(() -> messagePane.addMessage(messageContent, isSentByThisUser));
    }

    public void resetInputFields() {
        Platform.runLater(loginPane::resetInput);
        Platform.runLater(signInPane::resetInput);
        Platform.runLater(messagePane::resetInputAndFields);
    }

    public void setCurrentAddChatId(int id) {
        messagePane.setNewChatId(id);
    }
}
