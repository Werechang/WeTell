package com.gebb.wetell.client.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Objects;

public class LoginScene extends GridPane {

    private final Label disconnected;
    private final Button loginButton;
    private final TextField userTextField;
    private final PasswordField passwordField;
    private final Button changeToSignIn;
    private final Scene scene;

    public LoginScene() {
        super();
        this.setAlignment(Pos.CENTER);
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(25,25,25,25));

        scene = new Scene(this, 500, 300);
        scene.getStylesheets().add(Objects.requireNonNull(LoginScene.class.getResource("/com/gebb/wetell/stylesheets/login-dark.css")).toExternalForm());

        Label loginTitle = new Label("Login | OurChat");
        loginTitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        loginTitle.setId("title");
        this.add(loginTitle, 0, 0, 2, 1);

        Label userName = new Label("Username");
        this.add(userName, 0, 1);

        userTextField = new TextField();
        this.add(userTextField, 1, 1);

        Label passWord = new Label("Password");
        this.add(passWord, 0, 2);

        passwordField = new PasswordField();
        this.add(passwordField, 1, 2);

        CheckBox savePassword = new CheckBox("Save Username and Password");
        HBox checkBox = new HBox();
        checkBox.setAlignment(Pos.CENTER_LEFT);
        checkBox.getChildren().add(savePassword);
        this.add(checkBox, 1, 3);

        changeToSignIn = new Button("Sign in");
        changeToSignIn.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 14));
        changeToSignIn.setId("change");
        this.add(changeToSignIn, 1, 4);

        loginButton = new Button("Login");
        loginButton.setPrefSize(60, 10);
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(loginButton);
        this.add(buttonBox, 1, 5);

        disconnected = new Label("The Client is currently not connected to the Server");
        disconnected.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 10));
        disconnected.setVisible(false);
        this.add(disconnected, 1, 6);
    }

    public void setAsScene(Stage stage) {
        stage.setScene(scene);
    }

    public Label getDisconnectedLabel() {
        return disconnected;
    }

    public Button getLoginButton() {
        return loginButton;
    }

    public TextField getUserTextField() {
        return userTextField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getChangeSignInButton() {
        return changeToSignIn;
    }
}
