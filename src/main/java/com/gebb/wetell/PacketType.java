package com.gebb.wetell;

import org.jetbrains.annotations.NotNull;

public enum PacketType {
    UNKNOWN(-1),
    KEY(0),
    KEYREQUEST(1),
    LOGIN(2),
    MSG(3),
    ERROR_NO_DATA(4),
    ERROR_NO_ACCOUNT(5),
    ERROR_INVALID_LOGIN(6),
    ERROR_NOT_LOGGED_IN(7);

    private final byte id;

    /**
     *
     * @param id should be between 127 and -128, it gets cast into a byte
     */
    PacketType(int id) {
        this.id = (byte) id;
    }
    public byte getId() {
        return this.id;
    }

    public static @NotNull PacketType getTypeById(byte id) {
        for (PacketType p : values()) {
            if (p.id == id) return p;
        }
        return UNKNOWN;
    }
}