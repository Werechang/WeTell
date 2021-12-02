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

public class WeTellClient extends Application implements IConnectable, IGUICallable {

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private KeyPair keyPair;
    private PublicKey serverKey;
    private Thread listenThread;
    private Socket socket;
    private boolean isWaitingForConnection;
    private boolean isCloseRequest;
    private SceneManager sceneManager;

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

        sceneManager = new SceneManager(stage, this);
    }

    private void connect() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", 80));
            oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            oos.flush();
            ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            isWaitingForConnection = false;
            sendPacket(new PacketData(PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(keyPair.getPublic())));
            listen();
        } catch (IOException | NoSuchAlgorithmException e) {
            // if thread is not already running
            if (!isWaitingForConnection) {
                isWaitingForConnection = true;
                new Thread(() -> {
                    while (isWaitingForConnection && !isCloseRequest) {
                        try {
                            Thread.sleep(10000);
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
        listenThread = new Thread(() -> {
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
        });
        listenThread.start();
    }

    @Override
    public void onLoginPress(String username, String password) {
        // TODO Min length for uname and password
        if (username.isEmpty() || password.isEmpty() || username.length() < 8 || password.length() < 5) {
            return;
        }
        //TODO Attention Logic
    }

    @Override
    public void execPacket(PacketData data) {
        // TODO Implement actions
        if (data == null) {
            return;
        }
        switch (data.getType()) {
            case KEY -> {
                try {
                    serverKey = KeyPairManager.byteStreamToRSAPublicKey(data.getData());
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                sendPacket(new PacketData(PacketType.SUCCESS));
            }
            case MSG -> System.out.println(new String(data.getData(), StandardCharsets.UTF_8));
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

    public void prepareClose() {
        isCloseRequest = true;
        // TODO Say the Server that client dc's
        System.exit(0);
    }
}
