package com.gebb.wetell.client.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

// TODO create this in WeTellClient, add methods for changing to a specific scene (more information on monday)
public class SceneManager {
    
    private final LoginScene loginScene;
    private final Stage stage;
    
    public SceneManager(Stage stage) {
        this.stage = stage;
        // LoginScene
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(25,25,25,25));
        loginScene = new LoginScene(pane);

        // Login is the first scene
        setScene(SceneType.LOGIN);
        stage.show();
    }
    
    public void setScene(SceneType type) {
        switch (type) {
            case LOGIN -> stage.setScene(loginScene);
        }
    }
}
