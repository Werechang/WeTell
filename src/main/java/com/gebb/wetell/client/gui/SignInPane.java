package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SignInPane extends GridPane {

    public SignInPane(IGUICallable callable, SceneManager manager) {
        super();
        this.setAlignment(Pos.CENTER);
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(25,25,25,25));

        Label signInTitle = new Label("Sign In | WeTell");
        signInTitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        signInTitle.setId("title");
        this.add(signInTitle, 0, 0, 2, 1);

        Label username = new Label("Username");
        this.add(username, 0, 1);

        TextField userTextField = new TextField();
        this.add(userTextField, 1, 1);

        Label password = new Label("Password");
        this.add(password, 0, 2);

        PasswordField passwordField = new PasswordField();
        this.add(passwordField, 1, 2);

        CheckBox savePassword = new CheckBox("Save Username and Password");
        HBox checkBox = new HBox();
        checkBox.setAlignment(Pos.CENTER_LEFT);
        checkBox.getChildren().add(savePassword);
        this.add(checkBox, 1, 3);

        Button changeToLogin = new Button("Login");
        changeToLogin.setPrefSize(60, 10);
        changeToLogin.setPadding(new Insets(2, 5, 5, 5));
        changeToLogin.setAlignment(Pos.CENTER_LEFT);
        changeToLogin.setId("change");
        changeToLogin.setOnAction(event -> manager.setScene(SceneType.LOGIN));
        this.add(changeToLogin, 0, 4, 2, 1);

        Button signInButton = new Button("Sign In");
        signInButton.setPrefSize(60, 10);
        signInButton.setOnAction(event -> callable.onLoginPress(null, null));
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(signInButton);
        this.add(buttonBox, 1, 4);
        signInButton.setOnAction( event -> callable.onSignInPress(userTextField.getText(), passwordField.getText()));

        Label disconnected = new Label("The Client is currently not connected to the Server");
        disconnected.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 11));
        disconnected.setVisible(false);
        this.add(disconnected, 0, 6, 2, 1);
    }
}
