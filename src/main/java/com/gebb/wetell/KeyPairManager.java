package com.gebb.wetell;

import org.jetbrains.annotations.NotNull;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class KeyPairManager {
    /**
     * Generates an RSA keyPair of the length 4096 (bits). Use it for encrypting the message.
     * @return KeyPair. Access the keys with KeyPair.getPrivate() and KeyPair.getPublic()
     */
    public static @NotNull KeyPair generateRSAKeyPair() {
        KeyPairGenerator generator;
        // This algorithm should exist
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(4096);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new UnknownError("An error occurred while generating an RSA key pair.");
    }

    /**
     * Generates a DSA keyPair of the length 2048 (bits). Use it for a digital signature.
     * @return KeyPair. Access the keys with KeyPair.getPrivate() and KeyPair.getPublic()
     */
    public static @NotNull KeyPair generateDSAKeyPair() {
        KeyPairGenerator generator;

        try {
            generator = KeyPairGenerator.getInstance("DSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new UnknownError("An error occurred while generating a DSA key pair.");
    }

    /**
     * Sign contents of a Datapacket
     * @param data The data to generate a checksum of
     * @return ArrayList with the public key at index 0 and the signature at index 1
     */
    public static @NotNull ArrayList<byte[]> sign(byte[] data) {
        ArrayList<byte[]> buffer = new ArrayList<>(2);
        if (data == null) {
            throw new NullPointerException();
        }
        KeyPair keyPair = generateDSAKeyPair();
        buffer.add(keyPair.getPublic().getEncoded());
        try {
            Signature signature = Signature.getInstance("SHA256withDSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(data);
            buffer.add(signature.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     *
     * @param publicKey The key matching the private key used to calculate the signature.
     * @param data
     * @param signature
     * @return True if the checksum of the data matches the signature. Else false.
     */
    public static boolean checkSignature(@NotNull PublicKey publicKey, byte[] data, byte[] signature) {
        if (data == null || signature == null) {
            throw new NullPointerException();
        }
        try {
            Signature sign = Signature.getInstance("SHA256withDSA");
            sign.initVerify(publicKey);
            sign.update(data);
            return sign.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Serializes an RSA Public Key into a byte array. The output is encrypted.
     * @param key The key must not be null.
     * @return The byte array of the X509 encoded key.
     * @throws NoSuchAlgorithmException If the key does not support encoding.
     */
    public static byte[] RSAPublicKeyToByteStream(@NotNull PublicKey key) throws NoSuchAlgorithmException {
        if (key.getEncoded() == null) {
            // This should not happen
            throw new NoSuchAlgorithmException("The key does not support encoding");
        }
        return key.getEncoded();
    }

    /**
     * Deserializes a X509 encrypted key from a byte array into a PublicKey.
     * @param key The serialized key must not be null.
     * @return a PublicKey from the byte array.
     * @throws InvalidKeySpecException If the key is not encrypted with the X509 spec.
     */
    public static PublicKey byteStreamToRSAPublicKey(byte[] key) throws InvalidKeySpecException {
        if (key == null) {
            throw new NullPointerException("The key data should not be null");
        }
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        // This algorithm should exist
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new UnknownError();
    }

    public static PublicKey byteStreamToDSAPublicKey(byte[] key) throws InvalidKeySpecException {
        if (key == null) {
            throw new NullPointerException();
        }
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        // This algorithm should exist
        try {
            KeyFactory factory = KeyFactory.getInstance("DSA");
            return factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new UnknownError();
    }
}
