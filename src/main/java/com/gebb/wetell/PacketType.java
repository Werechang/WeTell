package com.gebb.wetell;

import org.jetbrains.annotations.NotNull;

public enum PacketType {
    /* Some are used on the server but not on the client and the other way around. They may have different meaning
     * depending on if it is the server or the client.
     */
    UNKNOWN(-1),
    KEY(0),
    KEYREQUEST(1),
    LOGIN(2),
    MSG(3),
    ERROR(4),
    LOGIN_SUCCESS(5),
    NOTIFICATION(6),
    CLOSE_CONNECTION(7),
    KEY_TRANSFER_SUCCESS(8),
    SIGNIN(9),
    LOGOUT(10),
    FETCH_MSGS(11),
    FETCH_CHATS(12),
    // Server side: add a new chat to the db | Client side: add chat information to the gui
    ADD_CHAT(13),
    ADD_USER_TO_CHAT(14),
    FETCH_MESSAGE(15),
    FETCH_CHAT(16),
    USER_ID(17);

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