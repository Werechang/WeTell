package com.gebb.wetell.dataclasses;

import com.gebb.wetell.connection.PacketType;
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
