package com.gebb.wetell;

import java.io.Serial;
import java.io.Serializable;

public class MessageData implements Serializable {
    @Serial
    private static final long serialVersionUID = -1722466964326836003L;
    private final int sentByUserId;
    private final int chatId;
    private final String msgContent;
    private final String sentAt;

    public MessageData(int sentByUserId, int chatId, String msgContent, String sentAt) {
        this.sentByUserId = sentByUserId;
        this.chatId = chatId;

        this.msgContent = msgContent;
        this.sentAt = sentAt;
    }

    public int getSentByUserId() {
        return sentByUserId;
    }

    public int getChatId() {
        return chatId;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getSentAt() {
        return sentAt;
    }
}
