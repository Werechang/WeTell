package com.gebb.wetell;

import com.gebb.wetell.client.WeTellClient;
import com.gebb.wetell.client.gui.SceneManager;
import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.Objects;

public class GUITest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("WeTell");
        stage.getIcons().add(new Image(Objects.requireNonNull(WeTellClient.class.getResource("gui/icons/wetell.png")).toExternalForm()));

        SceneManager sceneManager = new SceneManager(stage, null);
    }

    public static void initialize() {
        launch();
    }
}
