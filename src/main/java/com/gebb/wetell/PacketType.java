package com.gebb.wetell;

public enum PacketType {
    UNKNOWN(-1),
    KEY(0),
    LOGIN(1),
    MSG(2);

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

    public static PacketType getTypeById(byte id) {
        for (PacketType p : values()) {
            if (p.id == id) return p;
        }
        return UNKNOWN;
    }
}