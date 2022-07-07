package com.gebb.wetell.client.gui;

import com.gebb.wetell.client.IGUICallable;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessagePane extends GridPane {

    private final ChatList chatlist;
    private final UserInfoArea userInfoArea;
    private final ListView<Label> messageslist = new ListView<>();

    public MessagePane(IGUICallable callable, Stage stage) {
        this.setAlignment(Pos.CENTER);
        this.prefWidthProperty().bind(stage.widthProperty());
        this.prefHeightProperty().bind(stage.heightProperty());
        chatlist = new ChatList(callable);

        Region darkenRegion = new Region();
        darkenRegion.setMinWidth(100000);
        darkenRegion.setMinHeight(100000);
        darkenRegion.setId("darkenRegion");
        darkenRegion.setVisible(false);
        this.add(darkenRegion, 0, 0);

        userInfoArea = new UserInfoArea(callable, stage, darkenRegion);
        this.add(userInfoArea, 0, 0);
        // TODO Tweak around
        RowConstraints row0 = new RowConstraints(70, 70, 70);
        RowConstraints row1 = new RowConstraints(50, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
        row0.setVgrow(Priority.NEVER);
        row1.setVgrow(Priority.ALWAYS);
        ColumnConstraints col0 = new ColumnConstraints(200, 200, 400);
        ColumnConstraints col1 = new ColumnConstraints(600, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
        col0.setHgrow(Priority.ALWAYS);
        col1.setHgrow(Priority.SOMETIMES);
        this.getRowConstraints().addAll(row0, row1);
        this.getColumnConstraints().addAll(col0, col1);


        //Current contact Name + PB Area (1,0)
        HBox contactpbname = new HBox();
        contactpbname.setAlignment(Pos.CENTER_LEFT);
        contactpbname.setPadding(new Insets(10, 15, 10, 15));
        Circle contactpbView = new Circle(250, 250, 25);
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
        send.setOnAction(event -> {
            callable.onSendMessage(messageField.getText());
            messageField.setText("");
        });
        this.add(sendmessage, 1, 2);

        // TODO Remove -> to css
        chatlist.setStyle("-fx-background-color: #2f3c4c;");
        messageslist.setStyle("-fx-background-color: #292c2f;");
        sendmessage.setStyle("-fx-background-color: #292c2f;");
        darkenRegion.toFront();
    }

    protected void addChat(String name, int id) {
        HBox box = chatlist.chats.get(id);
        if (box != null) {
            // Update name+picture?
        } else {
            HBox chatpbname = new HBox();
            chatpbname.setAlignment(Pos.CENTER_LEFT);
            chatpbname.setPadding(new Insets(10, 15, 10, 15));
            Circle chatpbView = new Circle(250, 250, 25);
            Image chatpb = new Image(Objects.requireNonNull(MessagePane.class.getResource("icons/wetell.png")).toExternalForm());
            chatpbView.setFill(new ImagePattern(chatpb));
            Region region7 = new Region();
            region7.setPrefWidth(15);
            Label chatname = new Label(name);
            chatname.setAlignment(Pos.CENTER_LEFT);
            chatpbname.getChildren().addAll(chatpbView, region7, chatname);
            chatlist.addChat(id, new ChatBox(name));
        }
    }

    protected void addMessage(String message, boolean isSentByThisUser) {
        Label messageLabel = new Label(message);
        messageLabel.setTextAlignment(isSentByThisUser ? TextAlignment.RIGHT : TextAlignment.LEFT);
        messageslist.getItems().add(messageLabel);
    }

    protected void removeAllMessages() {
        messageslist.getItems().clear();
    }

    protected void resetInfo() {
        chatlist.removeAllChats();
        removeAllMessages();
        userInfoArea.resetInfo();
    }

    public void setNewChatId(int newChatId) {
        this.userInfoArea.popup.newChatId = newChatId;
        this.userInfoArea.popup.changeToAddUserScene();
    }

    public static class ChatBox extends HBox {
        public ChatBox(String name) {
            this.setAlignment(Pos.CENTER_LEFT);
            this.setPadding(new Insets(10, 15, 10, 15));
            Circle chatpbView = new Circle(250, 250, 25);
            Image chatpb = new Image(Objects.requireNonNull(MessagePane.class.getResource("icons/wetell.png")).toExternalForm());
            chatpbView.setFill(new ImagePattern(chatpb));
            Region region7 = new Region();
            region7.setPrefWidth(15);
            Label chatname = new Label(name);
            chatname.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().addAll(chatpbView, region7, chatname);
        }
    }

    public static class ChatList extends ListView<ChatBox> {
        private final HashMap<Integer, HBox> chats = new HashMap<>();

        public ChatList(IGUICallable callable) {
            this.setPrefWidth(300);
            this.setPrefHeight(590);
            this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                for (Map.Entry<Integer, HBox> entry : chats.entrySet()) {
                    if (Objects.equals(entry.getValue(), newValue)) {
                        callable.onSelectChat(entry.getKey());
                    }
                }
            });
        }

        public void addChat(int id, ChatBox box) {
            this.getItems().add(box);
            chats.put(id, box);
        }

        public void removeAllChats() {
            this.getItems().clear();
            chats.clear();
        }
    }

    public static class ChatDialogPopup extends Popup {
        private final GridPane chatDialogPaneAddChat = new GridPane();
        private final GridPane chatDialogPaneAddUsers = new GridPane();
        private int newChatId = -1;
        private final Label chatDescription = new Label("Enter new chat name");
        private final TextField chatName = new TextField();
        private final Button newChatButtonPopup = new Button("Create new chat");
        private final IGUICallable callable;

        public ChatDialogPopup(Region darkenRegion, IGUICallable callable) {
            this.callable = callable;
            newChatButtonPopup.setPrefWidth(150);
            newChatButtonPopup.setTextAlignment(TextAlignment.CENTER);

            chatDialogPaneAddChat.setId("newChat");
            chatDialogPaneAddChat.setPadding(new Insets(25, 25, 25, 25));
            chatDialogPaneAddChat.setVgap(10);
            chatDialogPaneAddChat.add(chatDescription, 0, 0);
            chatDialogPaneAddChat.add(chatName, 0, 1);
            chatDialogPaneAddChat.add(newChatButtonPopup, 0, 2);

            this.getContent().add(chatDialogPaneAddChat);
            this.setAutoHide(true);

            /*createnewchat.setOnAction(event -> callable.onAddChat(findcontact.getText()));
            entercontact.setOnAction(event -> callable.onAddUserToChat(newChatId, findcontact.getText()));
            close.setOnAction(event -> popup.hide());*/
            newChatButtonPopup.setOnAction(actionEvent -> {
                callable.onAddChat(chatName.getText());
                newChatButtonPopup.setVisible(false);
                chatName.setVisible(false);
                chatDescription.setVisible(false);
                chatName.setText("");
            });
            this.setOnAutoHide(event -> {
                chatName.setText("");
                newChatId = -1;
                darkenRegion.setVisible(false);
            });
        }

        public void changeToAddUserScene() {
            Platform.runLater(() -> {
                chatDescription.setText("Enter the name of a user you want to add");
                newChatButtonPopup.setText("Add user");
                newChatButtonPopup.setOnAction(actionEvent -> {
                    callable.onAddUserToChat(newChatId, chatName.getText());
                    chatName.setText("");
                });
                chatDescription.setVisible(true);
                chatName.setVisible(true);
                newChatButtonPopup.setVisible(true);
            });
        }
    }

    public static class UserInfoArea extends HBox {
        private final Label name = new Label();
        private final ChatDialogPopup popup;
        private final Stage stage;

        public UserInfoArea(IGUICallable callable, Stage stage, Region darkenRegion) {
            popup = new ChatDialogPopup(darkenRegion, callable);
            this.stage = stage;

            this.setAlignment(Pos.CENTER);
            this.setPadding(new Insets(10, 15, 10, 15));
            Circle userpbView = new Circle(250, 250, 25);
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
            this.getChildren().addAll(userpbView, name, region1, Logout, region2, newChatButton);

            newChatButton.setOnAction(event -> {
                popup.show(stage);
                darkenRegion.setVisible(true);
            });
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void resetInfo() {
            this.name.setText("");
        }
    }
}
