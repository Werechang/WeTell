package com.gebb.wetell;

import com.gebb.wetell.connection.Datapacket;
import com.gebb.wetell.connection.InvalidSignatureException;
import com.gebb.wetell.connection.PacketType;
import com.gebb.wetell.dataclasses.PacketData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class Tests {

    @Tag("data")
    @Test
    public void testCryptography() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidSignatureException {
        KeyPair kp = KeyPairManager.generateRSAKeyPair();
        Datapacket datapacket = new Datapacket(kp.getPublic(), PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(kp.getPublic()));
        PacketData pd = datapacket.getPacketData(kp.getPrivate());
        if (pd.getType() == PacketType.KEY) {
            PublicKey k = KeyPairManager.byteStreamToRSAPublicKey(pd.getData());
            Datapacket p = new Datapacket(k, PacketType.LOGIN, "WeTellUsNiceThings".getBytes(StandardCharsets.UTF_8));
            assertEquals(new String(p.getPacketData(kp.getPrivate()).getData(), StandardCharsets.UTF_8), "WeTellUsNiceThings");
        }
    }

    @Tag("data")
    @Test
    public void testHashing() {
        SecureRandom random = new SecureRandom();
        byte[] a = new byte[16];
        random.nextBytes(a);
        System.out.println(Arrays.toString(a));
    }

    @Tag("gui")
    @Test
    public void testGUI() {
        GUITest.initialize();
    }
}
