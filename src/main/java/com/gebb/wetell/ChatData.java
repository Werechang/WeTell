package com.gebb.wetell;

import java.io.Serial;
import java.io.Serializable;

public class ChatData implements Serializable {
    @Serial
    private static final long serialVersionUID = -8161318283622578807L;

    private final String name;
    private final int id;

    public ChatData(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
