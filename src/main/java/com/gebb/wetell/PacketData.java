package com.gebb.wetell;

import org.jetbrains.annotations.NotNull;

public class PacketData {

    private final PacketType type;
    private final byte[] data;

    /**
     * For Datapacket's getData()
     */
    public PacketData(@NotNull PacketType type, byte... data) {
        this.type = type;
        this.data = data;
    }

    public @NotNull PacketType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }
}
