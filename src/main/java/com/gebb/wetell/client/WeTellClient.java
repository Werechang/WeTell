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
import java.security.PublicKey;
import java.util.Objects;

public class WeTellClient extends Application implements IConnectable, IGUICallable {

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private KeyPair keyPair;
    private PublicKey serverKey;
    private Thread listenThread;
    private Socket socket;
    private boolean isWaitingForConnection;
    private boolean isCloseRequest;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // TODO Replace Thread.sleep with better interrupt
        // Init keys
        keyPair = KeyPairManager.generateRSAKeyPair();
        connect();
        // Window preparations
        stage.setTitle("WeTell");
        stage.getIcons().add(new Image(Objects.requireNonNull(WeTellClient.class.getResource("gui/icons/wetell.png")).toExternalForm()));

        SceneManager sceneManager = new SceneManager(stage, this);
    }

    private void connect() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", 21345));
            oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            oos.flush();
            ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            isWaitingForConnection = false;
            listen();
        } catch (IOException e) {
            // if thread is not already running
            if (!isWaitingForConnection) {
                isWaitingForConnection = true;
                new Thread(() -> {
                    while (isWaitingForConnection && !isCloseRequest) {
                        try {
                            Thread.sleep(1000);
                            connect();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
            e.printStackTrace();
        }
    }

    private void listen() {
        new Thread(() -> {
            while (!isWaitingForConnection && !isCloseRequest) {
                try {
                    Datapacket packet = (Datapacket) ois.readObject();
                    if (packet != null) {
                        execPacket(packet.getPacketData(null));
                    }
                    Thread.sleep(2000);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    // Check if connection got closed
                    if (e.getClass().getName().equals("java.io.EOFException") || e.getMessage().equals("Connection reset")) {
                        connect();
                    }
                } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void prepareClose() {
        isCloseRequest = true;
        if (listenThread != null) {
            try {
                listenThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoginPress(String username, String password) {
        //TODO Attention Logic
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
