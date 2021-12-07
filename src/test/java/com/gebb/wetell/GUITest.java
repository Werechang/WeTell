package com.gebb.wetell;

import com.gebb.wetell.client.IGUICallable;
import com.gebb.wetell.client.WeTellClient;
import com.gebb.wetell.client.gui.SceneManager;
import com.gebb.wetell.client.gui.SceneType;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class GUITest extends Application implements IGUICallable {

    private SceneManager sceneManager;

    @Override
    public void start(Stage stage) {
        stage.setTitle("WeTell");
        stage.getIcons().add(new Image(Objects.requireNonNull(WeTellClient.class.getResource("gui/icons/wetell.png")).toExternalForm()));

        sceneManager = new SceneManager(stage, this);
    }

    public static void initialize() {
        launch();
    }

    @Override
    public void prepareClose() {
        // Do nothing
    }

    @Override
    public void onLoginPress(String username, String password) {
        sceneManager.setScene(SceneType.MESSAGE);
    }
}
