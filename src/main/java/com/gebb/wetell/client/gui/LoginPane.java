package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginPane extends GridPane {
    private final TextField userTextField;
    private final PasswordField passwordField;
    private final Label disconnected = new Label("The Server is currently unavailable.");

    public LoginPane(IGUICallable callable, SceneManager manager) {
        super();
        this.setAlignment(Pos.CENTER);
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(25,25,25,25));

        Label loginTitle = new Label("Login | WeTell");
        loginTitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        loginTitle.setId("title");
        this.add(loginTitle, 0, 0, 2, 1);

        Label username = new Label("Username");
        this.add(username, 0, 1);

        userTextField = new TextField();
        this.add(userTextField, 1, 1);

        Label password = new Label("Password");
        this.add(password, 0, 2);

        passwordField = new PasswordField();
        this.add(passwordField, 1, 2);

        CheckBox savePassword = new CheckBox("Save Username and Password");
        HBox checkBox = new HBox();
        checkBox.setAlignment(Pos.CENTER_LEFT);
        checkBox.getChildren().add(savePassword);
        this.add(checkBox, 1, 3);

        Button changeToSignIn = new Button("Sign In");
        changeToSignIn.setPrefSize(60, 10);
        changeToSignIn.setPadding(new Insets(2, 5, 5, 5));
        changeToSignIn.setAlignment(Pos.CENTER_LEFT);
        changeToSignIn.setId("change");
        changeToSignIn.setOnAction(event -> manager.setScene(SceneType.SIGNIN));
        this.add(changeToSignIn, 0, 4, 2,1);

        Button loginButton = new Button("Login");
        loginButton.setPrefSize(60, 10);
        loginButton.setOnAction(event -> callable.onLoginPress(userTextField.getText(), passwordField.getText()));
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(loginButton);
        this.add(buttonBox, 1, 4);

        disconnected.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 11));
        disconnected.setVisible(false);
        this.add(disconnected, 0, 6, 2, 1);
    }

    protected void setDisconnectedLabel(boolean isVisible) {
        disconnected.setVisible(isVisible);
    }

    protected void resetInput() {
        userTextField.setText("");
        passwordField.setText("");
    }
}
