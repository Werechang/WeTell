package com.gebb.wetell;

import java.io.Serializable;
import java.security.PrivateKey;
import java.util.ArrayList;

public class Datapacket implements Serializable {

    private ArrayList<Byte> encryptedData;
    private boolean isEncrypted = true;

    public Datapacket(PrivateKey privateKey, PacketType type, byte... data) {
        if (privateKey == null || privateKey.isDestroyed()) {
            isEncrypted = false;
            encryptedData = new ArrayList<>(data.length);
            encryptedData.add(type.getId());
            for (byte b : data) {
                encryptedData.add(b);
            }
        } else {

        }
    }

    public ArrayList<Byte> getData() {
        if (!isEncrypted) {
            return encryptedData;
        }
        return null;
    }

    public enum PacketType {
        LOGIN(0),
        MSG(1);

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
    }
}
