package com.gebb.wetell;

import java.io.Serial;
import java.io.Serializable;

public class ChatData implements Serializable {
    @Serial
    private static final long serialVersionUID = -3822948789451697937L;

    private final String name;

    public ChatData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
