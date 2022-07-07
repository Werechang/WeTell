package com.gebb.wetell.client.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class MessageContainer extends GridPane {
    private final Label content;
    private final Label timestamp;

    public MessageContainer(String content, String timestamp) {
        this.content = new Label(content);
        this.timestamp = new Label(timestamp);
    }
}
