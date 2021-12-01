package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public class LoginScene extends Scene {

    public LoginScene(GridPane root, IGUICallable callable, SceneManager manager) {
        super(root);
        this.getStylesheets().add(Objects.requireNonNull(LoginScene.class.getResource("stylesheets/login-dark.css")).toExternalForm());

        Label loginTitle = new Label("Login | WeTell");
        loginTitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        loginTitle.setId("title");
        root.add(loginTitle, 0, 0, 2, 1);

        Label username = new Label("Username");
        //username.setMinWidth(Region.USE_PREF_SIZE); //label doesn't become smaller than content -> fix window size
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

        Button changeToSignIn = new Button("Sign In");
        changeToSignIn.setPrefSize(60, 10);
        changeToSignIn.setPadding(new Insets(2, 5, 5, 5));
        changeToSignIn.setAlignment(Pos.CENTER_LEFT);
        changeToSignIn.setId("change");
        changeToSignIn.setOnAction(event -> manager.setScene(SceneType.SIGNIN));
        root.add(changeToSignIn, 0, 4, 2,1);

        Button loginButton = new Button("Login");
        loginButton.setPrefSize(60, 10);
        loginButton.setOnAction(event -> callable.onLoginPress(null, null)); //TODO Attention Logic
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(loginButton);
        root.add(buttonBox, 1, 4);

        Label disconnected = new Label("The Client is currently not connected to the Server");
        disconnected.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 11));
        disconnected.setVisible(false);
        root.add(disconnected, 0, 6, 2, 1);
    }
}
