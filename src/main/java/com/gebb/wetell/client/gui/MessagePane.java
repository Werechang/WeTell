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
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Objects;

public class MessagePane extends GridPane {

    private final ListView<HBox> chatlist = new ListView<>();
    private final ListView<Label> messageslist = new ListView<>();
    private final GridPane newChatPane = new GridPane();
    private int newChatId = -1;
    private final HashMap<Integer, HBox> chats = new HashMap<>();

    public MessagePane(IGUICallable callable, Stage stage) {
        super();
        this.setAlignment(Pos.CENTER);

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
        Button newChatButton = new Button("+");
        newChatButton.setAlignment(Pos.CENTER_RIGHT);
        newChatButton.setMinWidth(Region.USE_PREF_SIZE);
        PbLogout.getChildren().addAll(userpbView, region1, Logout, region2, newChatButton);
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
        chatlist.setPrefWidth(300);
        chatlist.setPrefHeight(590);
        messageslist.setPrefWidth(700);
        messageslist.setPrefHeight(540);

        //Send Message Area (1,2)
        HBox sendmessage = new HBox();
        sendmessage.setAlignment(Pos.CENTER);
        sendmessage.setPadding(new Insets(8, 8, 8, 8));
        TextField messageField = new TextField();
        messageField.setAlignment(Pos.CENTER_LEFT);
        messageField.setPrefSize(600, 10);
        Region region4 = new Region();
        region4.setPrefWidth(8);
        Button send = new Button("Send");
        send.setAlignment(Pos.CENTER_LEFT);
        send.setMinWidth(Region.USE_PREF_SIZE);
        sendmessage.getChildren().addAll(messageField, region4, send);
        send.setOnAction(event -> callable.onSendMessage(messageField.getText()));
        this.add(sendmessage, 1, 2);

        //NewChatPane
        newChatPane.setAlignment(Pos.CENTER);
        HBox addcontact = new HBox();
        TextField findcontact = new TextField();
        Region region5 = new Region();
        region5.setPrefWidth(8);
        Button entercontact = new Button("Add");
        addcontact.getChildren().addAll(findcontact, region5, entercontact);
        Label added = new Label();
        HBox closecreate = new HBox();
        Button close = new Button("Cancel");
        Region region6 = new Region();
        region6.setPrefWidth(8);
        Button createnewchat = new Button("Create");
        closecreate.getChildren().addAll(close, region6, createnewchat);
        Popup popup = new Popup();
        newChatPane.add(addcontact, 0, 0);
        newChatPane.add(added, 0, 1);
        newChatPane.add(closecreate, 0, 2);
        popup.getContent().add(newChatPane);
        popup.setAutoHide(true);

        // TODO User other text field for chat name or hide the add button in the beginning and after creating the chat hide the create button
        createnewchat.setOnAction(event -> callable.onAddChat(findcontact.getText()));
        entercontact.setOnAction(event -> callable.onAddUserToChat(newChatId, findcontact.getText()));
        newChatButton.setOnAction(event -> popup.show(stage));
        close.setOnAction(event -> popup.hide());
        popup.setOnAutoHide(event -> {
            findcontact.setText("");
            newChatId = -1;
        });


        chatlist.setStyle("-fx-background-color: #2f3c4c;");
        messageslist.setStyle("-fx-background-color: #292c2f;");
        sendmessage.setStyle("-fx-background-color: #292c2f;");
        PbLogout.setStyle("-fx-background-color: #292c2f;");
    }

    protected void addChat(String name, int id) {
        HBox box = chats.get(id);
        if (box != null) {
            // Update name+picture?
        } else {
            HBox chatpbname = new HBox();
            chatpbname.setAlignment(Pos.CENTER_LEFT);
            chatpbname.setPadding(new Insets(10, 15, 10, 15));
            Circle chatpbView = new Circle(250,250,25);
            Image chatpb = new Image(Objects.requireNonNull(MessagePane.class.getResource("icons/wetell.png")).toExternalForm());
            chatpbView.setFill(new ImagePattern(chatpb));
            Region region7 = new Region();
            region7.setPrefWidth(15);
            Label chatname = new Label(name);
            chatname.setAlignment(Pos.CENTER_LEFT);
            chatpbname.getChildren().addAll(chatpbView, region7, chatname);
            chatlist.getItems().add(chatpbname);
            chats.put(id, chatpbname);
        }
    }

    protected void addMessage(String message, boolean isSentByThisUser) {
        Label messageLabel = new Label(message);
        messageLabel.setAlignment(isSentByThisUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messageslist.getItems().add(messageLabel);
    }

    protected void removeAllChats() {
        chatlist.getItems().clear();
        chats.clear();
    }

    protected void removeAllMessages() {
        messageslist.getItems().remove(0, messageslist.getItems().size()-1);
    }

    protected void resetInputAndFields() {
        removeAllChats();
        removeAllMessages();
    }

    public void setNewChatId(int newChatId) {
        this.newChatId = newChatId;
    }
}
