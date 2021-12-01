package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.WeTellClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.Objects;

public class SignInScene extends Scene {

    public SignInScene(GridPane root, WeTellClient client, SceneManager manager) {
        super(root);
        this.getStylesheets().add(Objects.requireNonNull(SignInScene.class.getResource("stylesheets/login-dark.css")).toExternalForm());

        Label signInTitle = new Label("Sign In | WeTell");
        signInTitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        signInTitle.setId("title");
        root.add(signInTitle, 0, 0, 2, 1);

        Label username = new Label("Username");
        root.add(username, 0, 1);

        TextField userTextField = new TextField();
        root.add(userTextField, 1, 1);

        Label password = new Label("Password");
        root.add(password, 0, 2);

        PasswordField passwordField = new PasswordField();
        root.add(passwordField, 1, 2);

        CheckBox savePassword = new CheckBox("Save Username and Password");
        HBox checkBox = new HBox();
        checkBox.setAlignment(Pos.CENTER_LEFT);
        checkBox.getChildren().add(savePassword);
        root.add(checkBox, 1, 3);

        Button changeToLogin = new Button("Login");
        changeToLogin.setPrefSize(60, 10);
        changeToLogin.setPadding(new Insets(2, 5, 5, 5));
        changeToLogin.setAlignment(Pos.CENTER_LEFT);
        changeToLogin.setId("change");
        changeToLogin.setOnAction(event -> manager.setScene(SceneType.LOGIN));
        root.add(changeToLogin, 0, 4, 2, 1);

        Button signInButton = new Button("Sign In");
        signInButton.setPrefSize(60, 10);
        signInButton.setOnAction(event -> manager.setScene(SceneType.MESSAGE)); //TODO Attention Logic
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(signInButton);
        root.add(buttonBox, 1, 4);

        Label disconnected = new Label("The Client is currently not connected to the Server");
        disconnected.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 11));
        disconnected.setVisible(false);
        root.add(disconnected, 0, 6, 2, 1);
    }
}
