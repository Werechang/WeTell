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

import java.util.Objects;

public class MessagePane extends GridPane {


    public MessagePane(IGUICallable callable, SceneManager manager) {
        super();
        this.setAlignment(Pos.CENTER);
        this.setGridLinesVisible(true);
        //this.getStylesheets().add(Objects.requireNonNull(MessagePane.class.getResource("stylesheets/login-dark.css")).toExternalForm());

        HBox PbLogout = new HBox();
        PbLogout.setAlignment(Pos.CENTER);
        Circle userpbView = new Circle(250,250,30);
        Image userpb = new Image(Objects.requireNonNull(MessagePane.class.getResource("icons/wetell.png")).toExternalForm());
        userpbView.setFill(new ImagePattern(userpb));
        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);
        Button Logout = new Button("Logout");
        Logout.setAlignment(Pos.CENTER_RIGHT);
        PbLogout.getChildren().addAll(userpbView, region1, Logout);
        this.add(PbLogout, 0, 0);

        HBox contactpbname = new HBox();
        contactpbname.setAlignment(Pos.CENTER);
        Circle contactpbView = new Circle(250,250,30);
        Image contactpb = new Image(Objects.requireNonNull(MessagePane.class.getResource("icons/wetell.png")).toExternalForm());
        contactpbView.setFill(new ImagePattern(contactpb));
        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);
        Label Contactname = new Label("Name");
        Contactname.setAlignment(Pos.CENTER_RIGHT);
        contactpbname.getChildren().addAll(contactpbView, region2, Contactname);
        this.add(contactpbname, 1, 0);

        HBox sendmessage = new HBox();
        TextField message = new TextField();
        message.setAlignment(Pos.CENTER_LEFT);
        Button send = new Button("Send");
        send.setAlignment(Pos.CENTER_RIGHT);
        sendmessage.getChildren().addAll(message, send);
        this.add(sendmessage, 1, 3);

    }
}