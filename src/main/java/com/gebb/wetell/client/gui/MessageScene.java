package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.WeTellClient;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MessageScene extends Scene {

    public MessageScene(GridPane root, WeTellClient client, SceneManager manager) {
        super(root);
        //this.getStylesheets().add(Objects.requireNonNull(MessageScene.class.getResource("stylesheets/login-dark.css")).toExternalForm());

        Label disconnected = new Label("The Client is currently not connected to the Server");
        disconnected.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 10));
        disconnected.setVisible(false);
        root.add(disconnected, 1, 6);
    }
}
