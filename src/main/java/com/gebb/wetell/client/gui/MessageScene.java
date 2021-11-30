package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.WeTellClient;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.Objects;

public class MessageScene extends Scene {

        private final Label disconnectedM;

        public MessageScene(GridPane root, WeTellClient client, SceneManager manager) {
            super(root, 800, 500);
            //this.getStylesheets().add(Objects.requireNonNull(com.gebb.wetell.client.gui.SignInScene.class.getResource("stylesheets/login-dark.css")).toExternalForm());

            disconnectedM = new Label("The Client is currently not connected to the Server");
            disconnectedM.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 10));
            disconnectedM.setVisible(false);
            root.add(disconnectedM, 1, 6);
        }

    public Label getDisconnectedLabel() {
        return disconnectedM;
    }

}
