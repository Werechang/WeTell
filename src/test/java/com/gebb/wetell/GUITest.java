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

    private final Label username;

    public GUITest(GridPane root, WeTellClient client, SceneManager manager) {
        super(root, 500, 300);

        Label username = new Label("Username");


}
