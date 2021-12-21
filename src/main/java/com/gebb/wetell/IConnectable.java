package com.gebb.wetell;

import com.gebb.wetell.dataclasses.PacketData;

public interface IConnectable {
    /**
     * Executes specific things depending on the PacketType
     * @param data The data + type that is used
     */
    void execPacket(PacketData data);

    /**
     * Send a packet through the OutputStream
     * @param data The data + type that is used
     */
    void sendPacket(PacketData data);

    /**
     * Send a packet through the OutputStream
     * @param data The data + type that is used
     */
    void sendPacket(PacketData data, boolean isEncrypted);

    boolean isLoggedInAndSecureConnection();

    void sendKey();
}
