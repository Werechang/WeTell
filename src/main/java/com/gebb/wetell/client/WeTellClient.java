package com.gebb.wetell.client;

import com.gebb.wetell.*;
import com.gebb.wetell.client.gui.SceneManager;
import com.gebb.wetell.client.gui.SceneType;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Objects;

public class WeTellClient extends Application implements IConnectable, IGUICallable {

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private KeyPair keyPair;
    private PublicKey serverKey = null;
    private Thread listenThread;
    private Socket socket;
    private boolean isWaitingForConnection;
    private boolean isCloseRequest;
    private boolean serverReceivedKey = false;
    private SceneManager sceneManager;
    private boolean hasResources = true;
    private int selectedChat = -1;
    private boolean isLoggedIn = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Init keys
        keyPair = KeyPairManager.generateRSAKeyPair();
        connect();
        // Window preparations
        stage.setTitle("WeTell");
        if (hasResources) {
            stage.getIcons().add(new Image(Objects.requireNonNull(WeTellClient.class.getResource("gui/icons/wetell.png")).toExternalForm()));
        }

        sceneManager = new SceneManager(stage, this, hasResources);
    }

    private void connect() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", 80));
            oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            oos.flush();
            ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            ois.setObjectInputFilter(new DatapacketFilter());
            isWaitingForConnection = false;
            sendPacket(new PacketData(PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(keyPair.getPublic())), false);
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
                        execPacket(packet.getPacketData(serverReceivedKey ? keyPair.getPrivate() : null));
                    }
                    Thread.sleep(2000);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    // Check if connection got closed
                    if (e.getClass().getName().equals("java.io.EOFException") || e.getMessage().equals("Connection reset")) {
                        connect();
                    }
                } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InterruptedException | InvalidSignatureException e) {
                    e.printStackTrace();
                }
            }
        });
        listenThread.start();
    }

    @Override
    public void onLoginPress(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return;
        }
        sendPacket(new PacketData(PacketType.LOGIN, (username + "\00" + password).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void onSignInPress(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty() || username.length() < 4 || password.length() < 4) {
            return;
        }
        sendPacket(new PacketData(PacketType.SIGNIN, (username + "\00" + password).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void onLogoutPress() {
        sendPacket(new PacketData(PacketType.LOGOUT));
        selectedChat = -1;
    }

    @Override
    public void onOpenPress() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        sendPacket(new PacketData(PacketType.ADD_CHAT));
    }

    @Override
    public void onSelectChat(int chatId) {
        selectedChat = chatId;
    }

    @Override
    public void onSendMessage(String content) {
        if (selectedChat != -1 && serverReceivedKey && isLoggedIn) {
            MessageData messageData = new MessageData(-1, selectedChat, content, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream msgOS = new ObjectOutputStream(bos);
                msgOS.writeObject(messageData);
                msgOS.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendPacket(new PacketData(PacketType.MSG, bos.toByteArray()));
        }
    }

    @Override
    public void onAddChat(String chatName) {
        if (serverReceivedKey && isLoggedIn) {
            ChatData chatData = new ChatData(chatName, -1);
        }
    }

    @Override
    public void execPacket(PacketData data) {
        if (data == null) {
            return;
        }
        switch (data.getType()) {
            case LOGIN_SUCCESS -> {
                sceneManager.setCurrentUserInformation(new String(data.getData(), StandardCharsets.UTF_8));
                sceneManager.setScene(SceneType.MESSAGE);
                isLoggedIn = true;
            }
            case KEY -> {
                try {
                    serverKey = KeyPairManager.byteStreamToRSAPublicKey(data.getData());
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                sendPacket(new PacketData(PacketType.KEY_TRANSFER_SUCCESS));
            }
            case MSG -> System.out.println(new String(data.getData(), StandardCharsets.UTF_8));
            case KEYREQUEST -> {
                try {
                    sendPacket(new PacketData(PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(keyPair.getPublic())));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            case KEY_TRANSFER_SUCCESS -> serverReceivedKey = true;
            case ERROR -> System.err.println("An error occurred while communicating with the server: " + new String(data.getData(), StandardCharsets.UTF_8));
            case ADD_CHAT -> {
                ByteArrayInputStream bis = new ByteArrayInputStream(data.getData());
                try {
                    ObjectInputStream is = new ObjectInputStream(bis);
                    is.setObjectInputFilter(new DatapacketFilter());
                    Object o = is.readObject();
                    // Instanceof checks to stay safe
                    if (o instanceof ChatData) {
                        ChatData chatData = (ChatData) o;
                        sceneManager.addChatInformation(chatData.getName(), chatData.getId());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            case FETCH_MSGS -> {
                ByteArrayInputStream bis = new ByteArrayInputStream(data.getData());
                try {
                    ObjectInputStream is = new ObjectInputStream(bis);
                    is.setObjectInputFilter(new DatapacketFilter());
                    Object o = is.readObject();
                    // Instanceof checks to stay safe
                    if (o instanceof ArrayList) {
                        ArrayList<?> messageData = (ArrayList<?>) o;
                        for (Object msg : messageData) {
                            if (msg instanceof MessageData) {
                                MessageData m = (MessageData) msg;
                                sceneManager.addMessage(m.getMsgContent(), m.getSentByUserId());
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            case FETCH_CHATS -> {
                ByteArrayInputStream bis = new ByteArrayInputStream(data.getData());
                try {
                    ObjectInputStream is = new ObjectInputStream(bis);
                    is.setObjectInputFilter(new DatapacketFilter());
                    Object o = is.readObject();
                    // Instanceof checks to stay safe
                    if (o instanceof ArrayList) {
                        ArrayList<?> chatData = (ArrayList<?>) o;
                        for (Object chat : chatData) {
                            if (chat instanceof ChatData) {
                                ChatData c = (ChatData) chat;
                                sceneManager.addChatInformation(c.getName(), c.getId());
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            case FETCH_USERNAME -> {
                // TODO What to do with it?
            }
            default -> System.err.println("PacketType " + data.getType() + " is either corrupted or currently not supported. Data: " + new String(data.getData(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void sendPacket(@NotNull PacketData data) {
        sendPacket(data, serverReceivedKey);
    }

    @Override
    public void sendPacket(PacketData data, boolean isEncrypted) {
        try {
            if (oos == null) {
                return;
            }
            oos.writeObject(new Datapacket(serverKey, data.getType(), isEncrypted, data.getData()));
            oos.flush();
            oos.reset();
        } catch (IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void prepareClose() {
        isCloseRequest = true;
        sendPacket(new PacketData(PacketType.CLOSE_CONNECTION));
        System.exit(0);
    }
}
