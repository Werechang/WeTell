package com.gebb.wetell.client;

import com.gebb.wetell.client.gui.SceneManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class WeTellClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("WeTell");
        stage.getIcons().add(new Image(Objects.requireNonNull(SceneManager.class.getResource("icons/wetell.png")).toExternalForm()));
        SceneManager sceneManager = new SceneManager(stage, this);
    }
}
