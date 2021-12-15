package com.gebb.wetell;

import java.io.Serial;
import java.io.Serializable;

public class MessageData implements Serializable {
    @Serial
    private static final long serialVersionUID = 6153905488799345979L;
    private final int sentByUserId;
    private final String msgContent;
    private final String sentAt;

    public MessageData(int sentByUserId, String msgContent, String sentAt) {
        this.sentByUserId = sentByUserId;

        this.msgContent = msgContent;
        this.sentAt = sentAt;
    }

    public int getSentByUserId() {
        return sentByUserId;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getSentAt() {
        return sentAt;
    }
}
