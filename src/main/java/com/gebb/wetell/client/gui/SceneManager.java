package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class SceneManager {
    
    private final LoginScene loginScene;
    private final SignInScene signInScene;
    private final MessageScene messageScene;
    private final Stage stage;
    
    public SceneManager(Stage stage, IGUICallable callable) {
        this.stage = stage;
        stage.setOnCloseRequest(event -> callable.prepareClose());
        stage.setHeight(700);
        stage.setWidth(1000);
        stage.setMinHeight(300);
        stage.setMinWidth(400);

        // LoginScene
        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(25,25,25,25));
        loginScene = new LoginScene(loginPane, callable, this);

        // SignInScene
        GridPane signInPane = new GridPane();
        signInPane.setAlignment(Pos.CENTER);
        signInPane.setHgap(10);
        signInPane.setVgap(10);
        signInPane.setPadding(new Insets(25,25,25,25));
        signInScene = new SignInScene(signInPane, callable, this);

        // MessageScene
        GridPane messagePane = new GridPane();
        messagePane.setAlignment(Pos.CENTER);
        messagePane.setGridLinesVisible(true);
        messageScene = new MessageScene(messagePane, callable, this);

        // Login is the first scene
        setScene(SceneType.LOGIN);
        stage.show();

        //TODO Stage Center = Center! ?
    }
    
    public void setScene(SceneType type) {
        switch (type) {
            case LOGIN -> stage.setScene(loginScene);
            case SIGNIN -> stage.setScene(signInScene);
            case MESSAGE -> stage.setScene(messageScene);
            default -> System.err.println("Scene not supported or set");
        }
    }
}
