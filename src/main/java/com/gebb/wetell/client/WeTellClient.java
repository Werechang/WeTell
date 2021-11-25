package com.gebb.wetell.client;

import com.gebb.wetell.*;
import com.gebb.wetell.client.gui.SceneManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

public class WeTellClient extends Application implements IConnectable {

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private KeyPair keyPair;
    private PublicKey serverKey;
    private Thread listenThread;
    private Socket socket;
    private boolean isWaitingForConnection = true;
    private boolean isReconnectThreadActive;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // TODO Find out why there is Exception spamming
        // Tests
        try {
            KeyPair kp = KeyPairManager.generateRSAKeyPair();
            Datapacket datapacket = new Datapacket(kp.getPublic(), PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(kp.getPublic()));
            PacketData pd = datapacket.getPacketData(kp.getPrivate());
            if (pd.getType() == PacketType.KEY) {
                PublicKey k = KeyPairManager.byteStreamToRSAPublicKey(pd.getData());
                Datapacket p = new Datapacket(k, PacketType.LOGIN, "WeTellUsNiceThings".getBytes(StandardCharsets.UTF_8));
                System.out.println("Packet data:" + new String(p.getPacketData(kp.getPrivate()).getData(), StandardCharsets.UTF_8));
            }
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        }
        // Init keys
        keyPair = KeyPairManager.generateRSAKeyPair();

        // Window preparations
        stage.setTitle("WeTell");
        stage.getIcons().add(new Image(Objects.requireNonNull(WeTellClient.class.getResource("gui/icons/wetell.png")).toExternalForm()));

        SceneManager sceneManager = new SceneManager(stage, this);
    }

    @Override
    public void execPacket(PacketData data) {
        // TODO Implement actions
        if (data == null) {
            return;
        }
        switch (data.getType()) {

        }
    }

    @Override
    public void sendPacket(@NotNull PacketData data) {
        try {
            oos.writeObject(new Datapacket(serverKey, data.getType(), data.getData()));
            oos.flush();
            oos.reset();
        } catch (IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
