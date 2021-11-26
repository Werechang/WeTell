package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.WeTellClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class SceneManager {
    
    private final LoginScene loginScene;
    private final SignInScene signInScene;
    private final Stage stage;
    
    public SceneManager(Stage stage, WeTellClient client) {
        this.stage = stage;
        // LoginScene
        GridPane loginPane = new GridPane();
        loginPane.setAlignment(Pos.CENTER);
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setPadding(new Insets(25,25,25,25));
        loginScene = new LoginScene(loginPane, client, this);

        // SignInScene
        GridPane signInPane = new GridPane();
        signInPane.setAlignment(Pos.CENTER);
        signInPane.setHgap(10);
        signInPane.setVgap(10);
        signInPane.setPadding(new Insets(25,25,25,25));
        signInScene = new SignInScene(signInPane, client, this);

        // Login is the first scene
        setScene(SceneType.LOGIN);
        stage.show();
    }
    
    public void setScene(SceneType type) {
        switch (type) {
            case LOGIN -> stage.setScene(loginScene);
            case SIGNIN -> stage.setScene(signInScene);
            case MESSAGE -> stage.setScene(null);
        }
    }
}
