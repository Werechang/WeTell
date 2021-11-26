package com.gebb.wetell;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class Tests {

    @Tag("data")
    @Test
    public void testCryptography() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {
        KeyPair kp = KeyPairManager.generateRSAKeyPair();
        Datapacket datapacket = new Datapacket(kp.getPublic(), PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(kp.getPublic()));
        PacketData pd = datapacket.getPacketData(kp.getPrivate());
        if (pd.getType() == PacketType.KEY) {
            PublicKey k = KeyPairManager.byteStreamToRSAPublicKey(pd.getData());
            Datapacket p = new Datapacket(k, PacketType.LOGIN, "WeTellUsNiceThings".getBytes(StandardCharsets.UTF_8));
            assertEquals(new String(p.getPacketData(kp.getPrivate()).getData(), StandardCharsets.UTF_8), "WeTellUsNiceThings");
        }
    }
}
