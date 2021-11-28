package com.gebb.wetell;

import org.jetbrains.annotations.NotNull;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class KeyPairManager {
    /**
     * Generates an RSA keyPair of the length 2048 (bits).
     * @return KeyPair. Access the keys with KeyPair.getPrivate() and KeyPair.getPublic()
     */
    public static @NotNull KeyPair generateRSAKeyPair() {
        KeyPairGenerator generator;
        // This algorithm should exist
        try {
            generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new UnknownError("An error occurred while generating an RSA key pair.");
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
    public static PublicKey byteStreamToRSAPublicKey(byte... key) throws InvalidKeySpecException {
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
}
