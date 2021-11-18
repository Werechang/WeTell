package com.gebb.wetell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Datapacket implements Serializable {

    private ArrayList<byte[]> encryptedData;

    public Datapacket(PrivateKey privateKey, PacketType type, byte... data) {
        if (privateKey == null || privateKey.isDestroyed()) {
            encryptedData = new ArrayList<>(data.length);
            encryptedData.add(new byte[]{type.getId()});
            encryptedData.add(data);
        } else {

        }
    }

    public PacketData getData(PublicKey publicKey) {
        if (publicKey == null) {
            // Tedious conversion from ArrayList of Object[] to one byte[]
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                for (Object o : encryptedData) {
                    oos.writeObject(o);
                }
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // get first position of first byte array, always the packetType id. Because there
            return new PacketData(PacketType.getTypeById(encryptedData.get(0)[0]), bos.toByteArray());
        }
        return null;
    }
}
