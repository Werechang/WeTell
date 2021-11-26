package com.gebb.wetell.client;

import com.gebb.wetell.Datapacket;
import com.gebb.wetell.IConnectable;
import com.gebb.wetell.KeyPairManager;
import com.gebb.wetell.PacketData;
import com.gebb.wetell.client.gui.SceneManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
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
