package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import java.util.Objects;

public class MessagePane extends GridPane {

    private final ListView<HBox> chatlist = new ListView<>();
    private final ListView<Label> messageslist = new ListView<>();

    public MessagePane(IGUICallable callable) {
        super();
        this.setAlignment(Pos.CENTER);
        this.setGridLinesVisible(true);

        //PB + Logout Area (0,0)
        HBox PbLogout = new HBox();
        PbLogout.setAlignment(Pos.CENTER);
        PbLogout.setPadding(new Insets(10, 15, 10, 15));
        Circle userpbView = new Circle(250,250,25);
        Image userpb = new Image(Objects.requireNonNull(MessagePane.class.getResource("icons/wetell.png")).toExternalForm());
        userpbView.setFill(new ImagePattern(userpb));
        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);
        Button Logout = new Button("Logout");
        Logout.setAlignment(Pos.CENTER_RIGHT);
        Logout.setOnAction(event -> callable.onLogoutPress());
        Logout.setMinWidth(Region.USE_PREF_SIZE);
        Region region2 = new Region();
        region2.setPrefWidth(8);
        Button NewChat = new Button("+");
        NewChat.setAlignment(Pos.CENTER_RIGHT);
        //NewChat.setOnAction(); TODO New Window/Stage
        NewChat.setMinWidth(Region.USE_PREF_SIZE);
        PbLogout.getChildren().addAll(userpbView, region1, Logout, region2, NewChat);
        this.add(PbLogout, 0, 0);

        //Current contact Name + PB Area (1,0)
        HBox contactpbname = new HBox();
        contactpbname.setAlignment(Pos.CENTER_LEFT);
        contactpbname.setPadding(new Insets(10, 15, 10, 15));
        Circle contactpbView = new Circle(250,250,25);
        Image contactpb = new Image(Objects.requireNonNull(MessagePane.class.getResource("icons/wetell.png")).toExternalForm());
        contactpbView.setFill(new ImagePattern(contactpb));
        Region region3 = new Region();
        region3.setPrefWidth(15);
        Label Contactname = new Label("Name");
        Contactname.setAlignment(Pos.CENTER_LEFT);
        Contactname.setFont(Font.font(14));
        contactpbname.getChildren().addAll(contactpbView, region3, Contactname);
        this.add(contactpbname, 1, 0);

        //Chat & Message List Area (0,1/1,1)
        this.add(chatlist, 0, 1, 1, 2);
        this.add(messageslist, 1, 1);

        //Send Message Area (1,2)
        HBox sendmessage = new HBox();
        sendmessage.setAlignment(Pos.CENTER);
        sendmessage.setPadding(new Insets(8, 8, 8, 8));
        TextField messageField = new TextField();
        messageField.setAlignment(Pos.CENTER_LEFT);
        messageField.setPrefSize(400, 10);
        Region region4 = new Region();
        region4.setPrefWidth(8);
        Button send = new Button("Send");
        send.setAlignment(Pos.CENTER_LEFT);
        send.setMinWidth(Region.USE_PREF_SIZE);
        sendmessage.getChildren().addAll(messageField, region4, send);
        send.setOnAction(event -> callable.onSendMessage(messageField.getText()));
        this.add(sendmessage, 1, 2);
    }

    protected void addChat(String name) {

        HBox chatpbname = new HBox();
        chatpbname.setAlignment(Pos.CENTER_LEFT);
        chatpbname.setPadding(new Insets(10, 15, 10, 15));
        Circle chatpbView = new Circle(250,250,25);
        Image chatpb = new Image(Objects.requireNonNull(MessagePane.class.getResource("icons/wetell.png")).toExternalForm());
        chatpbView.setFill(new ImagePattern(chatpb));
        Region region5 = new Region();
        region5.setPrefWidth(15);
        Label chatname = new Label(name);
        chatname.setAlignment(Pos.CENTER_LEFT);
        chatpbname.getChildren().addAll(chatpbView, region5, chatname);

        chatlist.getItems().add(chatpbname);
    }

    protected void addMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setAlignment(Pos.CENTER_RIGHT); //TODO Change Alignment left or right due sender_Id (if = logged in user_id)

        messageslist.getItems().add(messageLabel);
    }

    protected void resetInputAndFields() {

    }
}
