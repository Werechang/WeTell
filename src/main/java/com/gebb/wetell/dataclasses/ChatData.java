package com.gebb.wetell.dataclasses;

import java.io.Serial;
import java.io.Serializable;

public class ChatData implements Serializable {
    @Serial
    private static final long serialVersionUID = -8161318283622578807L;

    private final String name;
    private final int id;
    private final String salt;

    public ChatData(String name, int id, String salt) {
        this.name = name;
        this.id = id;
        this.salt = salt;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getSalt() {
        return salt;
    }
}
