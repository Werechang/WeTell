package com.gebb.wetell;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

public class Datapacket implements Serializable {
    @Serial
    private static final long serialVersionUID = -6234660021077765177L;

    private final ArrayList<byte[]> encryptedData = new ArrayList<>(4);
    private final boolean isEncrypted;

    private static Cipher cipher;
    private static final int SIGNATURE_KEY_POS = 0;
    private static final int SIGNATURE_HASH_POS = 1;
    private static final int PACKET_TYPE_POS = 2;
    private static final int DATA_POS = 3;

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
    public Datapacket(@Nullable PublicKey publicKey, @NotNull PacketType type, byte... data) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this(publicKey, type, true, data);
    }

    public Datapacket(@Nullable PublicKey publicKey, @NotNull PacketType type, boolean isEncrypted, byte... data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (publicKey == null || !isEncrypted) {
            this.isEncrypted = false;
            // Don't encrypt
            // Combine type and data
            bos.writeBytes(new byte[]{type.getId()});
            bos.writeBytes(data);

            ArrayList<byte[]> signature = KeyPairManager.sign(bos.toByteArray());
            encryptedData.add(signature.get(SIGNATURE_KEY_POS));
            encryptedData.add(signature.get(SIGNATURE_HASH_POS));
            encryptedData.add(new byte[]{type.getId()});
            if (data != null) {
                encryptedData.add(data);
            }
        } else {
            this.isEncrypted = true;
            // Store encrypted data in a bos
            // reserve pos 0 and 1 for the signature
            encryptedData.add(new byte[]{0});
            encryptedData.add(new byte[]{0});

            cipher.init(Cipher.PUBLIC_KEY, publicKey);
            // Add PacketType first
            int maxArraySize = 501;
            // Reserve a size, so we don't have to make memcpy's
            encryptedData.ensureCapacity(3 + (data.length/maxArraySize) + (data.length % maxArraySize == 0 ? 0 : 1));
            // Add the type
            byte[] encryptedType = cipher.doFinal(new byte[]{type.getId()});
            bos.writeBytes(encryptedType);
            encryptedData.add(encryptedType);
            // An asymmetric key cannot encrypt more than their length in bytes - 11.
            // That's why the data gets written into an ArrayList of byte arrays.
            for (int i = 0; i < data.length; i+= maxArraySize) {
                // Prevent sending null data by checking the remaining length
                if (i + maxArraySize > data.length) {
                    maxArraySize = data.length - i;
                }
                byte[] encryptedDataPart = cipher.doFinal(Arrays.copyOfRange(data, i, i+maxArraySize));
                bos.writeBytes(encryptedDataPart);
                encryptedData.add(encryptedDataPart);
            }
            ArrayList<byte[]> signature = KeyPairManager.sign(bos.toByteArray());
            encryptedData.set(SIGNATURE_KEY_POS, signature.get(SIGNATURE_KEY_POS));
            encryptedData.set(SIGNATURE_HASH_POS, signature.get(SIGNATURE_HASH_POS));
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
    public @NotNull PacketData getPacketData(@Nullable PrivateKey privateKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidSignatureException {
        if (!isEncrypted || privateKey == null || privateKey.isDestroyed()) {
            if (isEncrypted) {
                throw new NullPointerException("Packet is encrypted while the key is null");
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteArrayOutputStream dataBos = new ByteArrayOutputStream();
            bos.writeBytes(encryptedData.get(PACKET_TYPE_POS));
            for (int i = DATA_POS; i < encryptedData.size(); i++) {
                bos.writeBytes(encryptedData.get(i));
                dataBos.writeBytes(encryptedData.get(i));
            }
            // get first position of first byte array, always the packetType id. Because there
            boolean isSignatureValid = false;
            try {
                isSignatureValid = KeyPairManager.checkSignature(KeyPairManager.byteStreamToDSAPublicKey(encryptedData.get(SIGNATURE_KEY_POS)), bos.toByteArray(), encryptedData.get(SIGNATURE_HASH_POS));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
            if (!isSignatureValid) {
                throw new InvalidSignatureException();
            }
            return new PacketData(PacketType.getTypeById(encryptedData.get(PACKET_TYPE_POS)[0]), dataBos.toByteArray());
        } else {
            cipher.init(Cipher.PRIVATE_KEY, privateKey);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteArrayOutputStream signatureBos = new ByteArrayOutputStream();
            signatureBos.writeBytes(encryptedData.get(PACKET_TYPE_POS));
            // Starting at 3 because of PacketType being at 2 and signature in range 0-1
            for (int i = DATA_POS; i < encryptedData.size(); i++) {
                bos.writeBytes(cipher.doFinal(encryptedData.get(i)));
                signatureBos.writeBytes(encryptedData.get(i));
            }
            boolean isSignatureValid = false;
            try {
                isSignatureValid = KeyPairManager.checkSignature(KeyPairManager.byteStreamToDSAPublicKey(encryptedData.get(SIGNATURE_KEY_POS)), signatureBos.toByteArray(), encryptedData.get(SIGNATURE_HASH_POS));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
            if (!isSignatureValid) {
                throw new InvalidSignatureException();
            }
            return new PacketData(PacketType.getTypeById(cipher.doFinal(encryptedData.get(PACKET_TYPE_POS))[0]), bos.toByteArray());
        }
    }
}
