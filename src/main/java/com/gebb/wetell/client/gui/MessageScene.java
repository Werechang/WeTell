package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class MessageScene extends Scene {


    public MessageScene(GridPane root, IGUICallable callable, SceneManager manager) {
        super(root);
        //this.getStylesheets().add(Objects.requireNonNull(MessageScene.class.getResource("stylesheets/login-dark.css")).toExternalForm());

        HBox PbLogout = new HBox();
        Circle userpbView = new Circle(250,250,120);
        Image userpb = new Image("https://karrierebibel.de/wp-content/uploads/2017/02/Profilbild-Tipp-Bildausschnitt.jpg");
        userpbView.setFill(new ImagePattern(userpb));
        Button Logout = new Button("Logout");
        Logout.setAlignment(Pos.CENTER_RIGHT);
        PbLogout.getChildren().addAll(userpbView, Logout);
        root.add(PbLogout, 0, 0);

        //TODO Achtung nächstes aus irgendeinem Grund noch nicht an richtiger Position
        HBox contactpbname = new HBox();
        Circle contactpbView = new Circle(250,250,120);
        Image contactpb = new Image("https://karrierebibel.de/wp-content/uploads/2017/02/Profilbild-Tipp-Bildausschnitt.jpg");
        contactpbView.setFill(new ImagePattern(contactpb));
        Label Contactname = new Label("Name");
        PbLogout.getChildren().addAll(contactpbView, Contactname);
        root.add(contactpbname, 1, 0);

        HBox sendmessage = new HBox();
        TextField message = new TextField();
        message.setAlignment(Pos.CENTER_LEFT);
        Button send = new Button("Send");
        send.setAlignment(Pos.CENTER_RIGHT);
        sendmessage.getChildren().addAll(message, send);
        root.add(sendmessage, 1, 2);

    }
}
