package com.gebb.wetell;

public class PacketData {

    private final PacketType type;
    private final byte[] data;

    /**
     * For Datapacket's getData()
     * @param type
     * @param data
     */
    public PacketData(PacketType type, byte... data) {
        this.type = type;
        this.data = data;
    }

    public PacketType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }
}
