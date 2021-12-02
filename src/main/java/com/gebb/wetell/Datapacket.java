package com.gebb.wetell;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;

public class Datapacket implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    private final ArrayList<byte[]> encryptedData = new ArrayList<>(2);
    private static Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * This object is for managing data with their respective types (PacketTypes). It generates an ArrayList
     * of encrypted byte arrays. If you want to retrieve the data, call getPacketData().
     * @param publicKey Generate a keypair with the KeyPairManager [KeyPairManager.generateRSAKeyPair()] and pass over
     *                  a reference to the PublicKey [KeyPair.getPublic()]. If you generate the KeyPair by your own,
     *                  initialize it with a size of 2048 bits. If you pass over null, the data does not get encrypted.
     *                  The only case this should be used is on key exchange. Be careful to call getPacketData() with
     *                  a PrivateKey that is null.
     * @param type Check the enum PacketType for further description.
     * @param data The data you want to transfer. You have to pass a byte array. If you want to pass an Object, you should
     *             write serialization/deserialization helper methods in your Objects class.
     * @throws InvalidKeyException This should not happen. Make sure that you generated a PublicKey with the KeyPairManager.
     */
    public Datapacket(@Nullable PublicKey publicKey, @NotNull PacketType type, byte... data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (publicKey == null) {
            // Don't encrypt
            encryptedData.add(new byte[]{type.getId()});
            encryptedData.add(data);
        } else {
            cipher.init(Cipher.PUBLIC_KEY, publicKey);
            // Add PacketType first
            int maxArraySize = 501;
            // Reserve a size, so we don't have to make memcpy's
            encryptedData.ensureCapacity(1 + (data.length/maxArraySize) + (data.length % maxArraySize == 0 ? 0 : 1));
            // Add the type
            encryptedData.add(cipher.doFinal(new byte[]{type.getId()}));
            // An asymmetric key cannot encrypt more than their length in bytes - 11.
            // That's why the data gets written into an ArrayList of byte arrays.
            for(int i = 0; i < data.length; i+= maxArraySize) {
                // Prevent sending null data by checking the remaining length
                if (i + maxArraySize > data.length) {
                    maxArraySize = data.length - i;
                }
                encryptedData.add(cipher.doFinal(Arrays.copyOfRange(data, i, i+maxArraySize)));
            }
        }
    }

    /**
     * Retrieve the data of the DataPacket
     * @param privateKey Make sure to use the PrivateKey that belongs to the PublicKey passed into the constructor of
     *                   this object. Use null if the constructor was called with a null PublicKey.
     * @return An Object that contains the PacketType and the (decrypted) data of the packet.
     * @throws InvalidKeyException This should not happen. Make sure to use the PrivateKey that belongs to the
     *                             PublicKey passed into the constructor of this object.
     */
    public @NotNull PacketData getPacketData(@Nullable PrivateKey privateKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (privateKey == null || privateKey.isDestroyed()) {
            // Tedious conversion from ArrayList of Object[] to one byte[]
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            for (int i = 1; i < encryptedData.size(); i++) {
                bos.writeBytes(encryptedData.get(i));
            }
            // get first position of first byte array, always the packetType id. Because there
            return new PacketData(PacketType.getTypeById(encryptedData.get(0)[0]), bos.toByteArray());
        } else {
            cipher.init(Cipher.PRIVATE_KEY, privateKey);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // Starting at 1 because of PacketType being at 0
            for (int i = 1; i < encryptedData.size(); i++) {
                bos.writeBytes(cipher.doFinal(encryptedData.get(i)));
            }
            return new PacketData(PacketType.getTypeById(cipher.doFinal(encryptedData.get(0))[0]), bos.toByteArray());
        }
    }
}
