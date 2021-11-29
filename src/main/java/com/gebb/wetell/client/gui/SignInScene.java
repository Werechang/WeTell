package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.WeTellClient;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public class SignInScene extends Scene {

    private final Label disconnectedS;
    private final Button signInButton;
    private final TextField userTextFieldS;
    private final PasswordField passwordFieldS;
    private final Button changeToLogin;

    public SignInScene(GridPane root, WeTellClient client, SceneManager manager) {
        super(root, 500, 300);
        this.getStylesheets().add(Objects.requireNonNull(SignInScene.class.getResource("stylesheets/login-dark.css")).toExternalForm());

        Label signInTitle = new Label("Sign In | WeTell");
        signInTitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        signInTitle.setId("title");
        root.add(signInTitle, 0, 0, 2, 1);

        Label userName = new Label("Username");
        root.add(userName, 0, 1);

        userTextFieldS = new TextField();
        root.add(userTextFieldS, 1, 1);

        Label passWord = new Label("Password");
        root.add(passWord, 0, 2);

        passwordFieldS = new PasswordField();
        root.add(passwordFieldS, 1, 2);

        /*CheckBox savePassword = new CheckBox("Save Username and Password");
        HBox checkBox = new HBox();
        checkBox.setAlignment(Pos.CENTER_LEFT);
        checkBox.getChildren().add(savePassword);
        root.add(checkBox, 1, 3);*/
       
        changeToLogin = new Button("Login");
        changeToLogin.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 14));
        changeToLogin.setAlignment(Pos.CENTER_LEFT);
        changeToLogin.setId("change");
        changeToLogin.setOnAction(event -> manager.setScene(SceneType.LOGIN));
        root.add(changeToLogin, 0, 4);

        signInButton = new Button("Sign In");
        signInButton.setPrefSize(60, 10);
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(signInButton);
        root.add(buttonBox, 1, 4);

        disconnectedS = new Label("The Client is currently not connected to the Server");
        disconnectedS.setFont(Font.font("SegoeUI", FontWeight.NORMAL, 10));
        disconnectedS.setVisible(false);
        root.add(disconnectedS, 1, 6);
    }

    public Label getDisconnectedLabelS() {
        return disconnectedS;
    }

    public Button getSignInButton() {
        return signInButton;
    }

    public TextField getUserTextFieldS() {
        return userTextFieldS;
    }

    public PasswordField getPasswordFieldS() {
        return passwordFieldS;
    }

    public Button getChangeToLogin() {
        return changeToLogin;
    }
}
