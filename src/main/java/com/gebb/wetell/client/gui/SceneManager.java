package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
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

        // LoginPane
        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(25,25,25,25));
        this.loginPane = new LoginPane(callable, this);

        // SignInPane
        GridPane signInPane = new GridPane();
        signInPane.setAlignment(Pos.CENTER);
        signInPane.setHgap(10);
        signInPane.setVgap(10);
        signInPane.setPadding(new Insets(25,25,25,25));
        this.signInPane = new SignInPane(callable, this);

        // MessagePane
        GridPane messagePane = new GridPane();
        messagePane.setAlignment(Pos.CENTER);
        messagePane.setGridLinesVisible(true);
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
