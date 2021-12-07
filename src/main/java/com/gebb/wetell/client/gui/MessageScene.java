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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class MessageScene extends Scene {


    public MessageScene(GridPane root, IGUICallable callable, SceneManager manager) {
        super(root);
        //this.getStylesheets().add(Objects.requireNonNull(MessageScene.class.getResource("stylesheets/login-dark.css")).toExternalForm());

        HBox PbLogout = new HBox();
        PbLogout.setAlignment(Pos.CENTER);
        Circle userpbView = new Circle(250,250,30);
        Image userpb = new Image("https://karrierebibel.de/wp-content/uploads/2017/02/Profilbild-Tipp-Bildausschnitt.jpg");
        userpbView.setFill(new ImagePattern(userpb));
        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);
        Button Logout = new Button("Logout");
        Logout.setAlignment(Pos.CENTER_RIGHT);
        PbLogout.getChildren().addAll(userpbView, region1, Logout);
        root.add(PbLogout, 0, 0);

        HBox contactpbname = new HBox();
        contactpbname.setAlignment(Pos.CENTER);
        Circle contactpbView = new Circle(250,250,30);
        Image contactpb = new Image("https://karrierebibel.de/wp-content/uploads/2017/02/Profilbild-Tipp-Bildausschnitt.jpg");
        contactpbView.setFill(new ImagePattern(contactpb));
        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);
        Label Contactname = new Label("Name");
        Contactname.setAlignment(Pos.CENTER_RIGHT);
        contactpbname.getChildren().addAll(contactpbView, region2, Contactname);
        root.add(contactpbname, 1, 0);

        HBox sendmessage = new HBox();
        TextField message = new TextField();
        message.setAlignment(Pos.CENTER_LEFT);
        Button send = new Button("Send");
        send.setAlignment(Pos.CENTER_RIGHT);
        sendmessage.getChildren().addAll(message, send);
        root.add(sendmessage, 1, 3);

    }
}
