package com.gebb.wetell.dataclasses;

import java.io.Serial;
import java.io.Serializable;

public class ContactData implements Serializable {
    @Serial
    private static final long serialVersionUID = -7062941202251305507L;

    private final int chatId;
    private final int userId;

    public ContactData(int chatId, int userId) {
        this.chatId = chatId;
        this.userId = userId;
    }

    public int getChatId() {
        return chatId;
    }

    public int getUserId() {
        return userId;
    }
}
