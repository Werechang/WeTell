package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class SceneManager {
    
    private final LoginPane loginPane;
    private final SignInPane signInPane;
    private final MessagePane messagePane;
    private final Scene scene;

    public SceneManager(Stage stage, IGUICallable callable) {
        stage.setOnCloseRequest(event -> callable.prepareClose());
        stage.setHeight(700);
        stage.setWidth(1000);
        stage.setMinHeight(300);
        stage.setMinWidth(400);

        this.loginPane = new LoginPane(callable, this);
        this.signInPane = new SignInPane(callable, this);
        this.messagePane = new MessagePane(callable, this);

        // Login is the first scene
        this.scene = new Scene(this.loginPane);
        scene.getStylesheets().add(Objects.requireNonNull(LoginPane.class.getResource("stylesheets/login-dark.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public void setScene(SceneType type) {
        switch (type) {
            case LOGIN -> {
                scene.getStylesheets().set(0, Objects.requireNonNull(SignInPane.class.getResource("stylesheets/login-dark.css")).toExternalForm());
                scene.setRoot(loginPane);
            }
            case SIGNIN -> {
                scene.getStylesheets().set(0, Objects.requireNonNull(SignInPane.class.getResource("stylesheets/login-dark.css")).toExternalForm());
                scene.setRoot(signInPane);
            }
            case MESSAGE -> {
                scene.getStylesheets().set(0, Objects.requireNonNull(SignInPane.class.getResource("stylesheets/login-dark.css")).toExternalForm());
                scene.setRoot(messagePane);
            }
            default -> System.err.println("Scene not supported or set");
        }
    }
}
