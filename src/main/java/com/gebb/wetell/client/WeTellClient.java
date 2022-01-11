package com.gebb.wetell.client;

import com.gebb.wetell.*;
import com.gebb.wetell.client.gui.SceneManager;
import com.gebb.wetell.client.gui.SceneType;
import com.gebb.wetell.connection.Datapacket;
import com.gebb.wetell.connection.DatapacketFilter;
import com.gebb.wetell.connection.InvalidSignatureException;
import com.gebb.wetell.connection.PacketType;
import com.gebb.wetell.dataclasses.*;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class WeTellClient extends Application implements IConnectable, IGUICallable {

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private KeyPair keyPair;
    private PublicKey serverKey = null;
    private boolean isWaitingForConnection;
    private boolean isCloseRequest;
    private boolean serverReceivedKey = false;
    private SceneManager sceneManager;
    private final boolean hasResources = true;
    private int selectedChat = -1;
    private boolean isLoggedIn = false;
    private int userId = -1;
    private final CountDownLatch getUserIdWaiter = new CountDownLatch(1);
    private int requestedUserId = -1;
    private byte[] userSecret = new byte[256];

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Init keys
        keyPair = KeyPairManager.generateRSAKeyPair();
        // Window preparations
        stage.setTitle("WeTell");
        if (hasResources) {
            stage.getIcons().add(new Image(Objects.requireNonNull(WeTellClient.class.getResource("gui/icons/wetell.png")).toExternalForm()));
        }

        sceneManager = new SceneManager(stage, this, hasResources);
        connect();
    }

    private void connect() {
        new Thread(() -> {
            try {
                Socket socket = new Socket();
                sceneManager.setDisconnected(true);
                socket.connect(new InetSocketAddress("192.168.178.127", 24464));
                oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                oos.flush();
                ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                ois.setObjectInputFilter(new DatapacketFilter());
                isWaitingForConnection = false;
                sceneManager.setDisconnected(false);
                sendPacket(new PacketData(PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(keyPair.getPublic())), false);
                listen();
            } catch (IOException | NoSuchAlgorithmException e) {
                // if thread is not already running
                if (!isWaitingForConnection) {
                    isWaitingForConnection = true;
                    sceneManager.setDisconnected(true);
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
        }).start();
    }

    private void listen() {
        // Check if connection got closed
        new Thread(() -> {
            while (!isWaitingForConnection && !isCloseRequest) {
                try {
                    Datapacket packet = (Datapacket) ois.readObject();
                    if (packet != null) {
                        execPacket(packet.getPacketData(serverReceivedKey ? keyPair.getPrivate() : null));
                    }
                    Thread.sleep(10);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    // Check if connection got closed
                    if (e.getClass().getName().equals("java.io.EOFException") || e.getMessage().equals("Connection reset")) {
                        if (!isWaitingForConnection && !isCloseRequest) {
                            connect();
                        }
                    }
                } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InterruptedException | InvalidSignatureException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onLoginPress(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return;
        }
        try {
            userSecret = getUserSecret(username, password);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sendPacket(new PacketData(PacketType.LOGIN, (username + "\00" + password).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void onSignInPress(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty() || username.length() < 4 || password.length() < 4) {
            return;
        }
        try {
            userSecret = getUserSecret(username, password);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sendPacket(new PacketData(PacketType.SIGNIN, (username + "\00" + password).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void onLogoutPress() {
        sendPacket(new PacketData(PacketType.LOGOUT));
        selectedChat = -1;
        isLoggedIn = false;
        userId = -1;
    }

    @Override
    public void onSelectChat(int chatId) {
        sceneManager.clearMessageList();
        if (isLoggedInAndSecureConnection()) {
            selectedChat = chatId;
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(chatId);
            sendPacket(new PacketData(PacketType.FETCH_MSGS, buffer.array()));
        }
    }

    @Override
    public void onSendMessage(String content) {
        if (isLoggedInAndSecureConnection() && selectedChat != -1) {
            sendPacket(new PacketData(PacketType.MSG, Util.serializeObject(new MessageData(-1, selectedChat, content, null))));
        }
    }

    @Override
    public void onAddChat(String chatName) {
        if (isLoggedInAndSecureConnection()) {
            sendPacket(new PacketData(PacketType.ADD_CHAT, Util.serializeObject(new ChatData(chatName, -1, null))));
        }
    }

    @Override
    public void onAddUserToChat(int chatId, String username) {
        if (isLoggedInAndSecureConnection() && chatId != -1 && username.length() >= 4) {
            sendPacket(new PacketData(PacketType.USER_ID, username.getBytes(StandardCharsets.UTF_8)));
            new Thread(() -> {
                try {
                    getUserIdWaiter.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (requestedUserId != -1) {
                    sendPacket(new PacketData(PacketType.ADD_USER_TO_CHAT, Util.serializeObject(new ContactData(chatId, requestedUserId))));
                } // Else: user does not exist
            }).start();
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
                sendPacket(new PacketData(PacketType.FETCH_CHATS));
            }
            case KEY -> {
                try {
                    serverKey = KeyPairManager.byteStreamToRSAPublicKey(data.getData());
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                sendPacket(new PacketData(PacketType.KEY_TRANSFER_SUCCESS));
            }
            case MSG -> {

            }
            case KEYREQUEST -> sendKey();
            case KEY_TRANSFER_SUCCESS -> serverReceivedKey = true;
            case ERROR -> System.err.println("An error occurred while communicating with the server: " + new String(data.getData(), StandardCharsets.UTF_8));
            case FETCH_CHATS -> addChatDataArray(data.getData());
            case FETCH_CHAT -> addChatData(data.getData());
            case FETCH_MSGS -> addMessageDataArray(data.getData());
            case FETCH_MESSAGE -> addMessageData(data.getData());
            case USER_ID -> userId = ByteBuffer.wrap(data.getData()).getInt();
            case LOGOUT -> {
                isLoggedIn = false;
                userId = -1;
                selectedChat = -1;
                sceneManager.resetInputFields();
                sceneManager.setScene(SceneType.LOGIN);
                for (byte b: userSecret) {
                    b = 0;
                }
            }
            case USERID_FROM_NAME -> {
                requestedUserId = ByteBuffer.wrap(data.getData()).getInt();
                getUserIdWaiter.countDown();
            }
            case ADD_CHAT_SUCCESS -> sceneManager.setCurrentAddChatId(ByteBuffer.wrap(data.getData()).getInt());
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
        System.out.println("Sent packet: " + data.getType());
    }

    @Override
    public boolean isLoggedInAndSecureConnection() {
        if (!isLoggedIn) {
            System.err.println("You are not logged in.");
            return false;
        }
        if (!serverReceivedKey) {
            sendKey();
            return false;
        }
        if (serverKey == null) {
            sendPacket(new PacketData(PacketType.KEYREQUEST), false);
            return false;
        }
        return true;
    }

    @Override
    public void sendKey() {
        try {
            sendPacket(new PacketData(PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(keyPair.getPublic())), false);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void addChatData(byte[] data) {
        if (isLoggedInAndSecureConnection()) {
            ChatData chatData = Util.deserializeObject(ChatData.class, data);
            if (chatData != null) {
                sceneManager.addChatInformation(chatData.getName(), chatData.getId());
            }
        }
    }

    private void addChatDataArray(byte[] data) {
        if (isLoggedInAndSecureConnection()) {
            ArrayList<?> chatDataArrayList = Util.deserializeObject(ArrayList.class, data);
            if (chatDataArrayList != null) {
                for (Object o: chatDataArrayList) {
                    if (o instanceof ChatData) {
                        ChatData chatData = (ChatData) o;
                        sceneManager.addChatInformation(chatData.getName(), chatData.getId());
                    }
                }
            }
        }
    }

    private void addMessageDataArray(byte[] data) {
        if (isLoggedInAndSecureConnection()) {
            ArrayList<?> messageDataArrayList = Util.deserializeObject(ArrayList.class, data);
            if (messageDataArrayList != null) {
                for (Object o: messageDataArrayList) {
                    if (o instanceof MessageData) {
                        MessageData messageData = (MessageData) o;
                        if (messageData.getChatId() == selectedChat) {
                            sceneManager.addMessage(messageData.getMsgContent(), messageData.getSentByUserId() == userId);
                        }
                    }
                }
            }
        }
    }

    private void addMessageData(byte[] data) {
         if (isLoggedInAndSecureConnection()) {
             MessageData messageData = Util.deserializeObject(MessageData.class, data);
             if (messageData != null) {
                 if (selectedChat == messageData.getChatId()) {
                     sceneManager.addMessage(messageData.getMsgContent(), messageData.getSentByUserId() == userId);
                 }
             }
         }
    }

    public void prepareClose() {
        isCloseRequest = true;
        sendPacket(new PacketData(PacketType.CLOSE_CONNECTION));
        System.exit(0);
    }

    private byte[] encryptMessage(String message, SecretKey key) {
        PBEKeySpec spec = new PBEKeySpec(message.toCharArray(), null, 65536, 256);
        return null;
    }

    private String decryptMessage(byte[] message, SecretKey key) {
        return null;
    }

    private byte[] getUserSecret(String username, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom(password.getBytes(StandardCharsets.UTF_8));
        byte[] seededRnd = new byte[16];
        random.nextBytes(seededRnd);
        PBEKeySpec spec = new PBEKeySpec(username.toCharArray(), seededRnd, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }

    private void generateNewChatKey() {
        // Generating the password for the key (chat secret)
        SecureRandom random = new SecureRandom();
        byte[] chatSecret = new byte[256];
        random.nextBytes(chatSecret);
        // TODO Generate on server
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            // String wrapping workaround to convert the byte[] into a char[]
            PBEKeySpec spec = new PBEKeySpec(new String(chatSecret, StandardCharsets.UTF_8).toCharArray(), salt, 65536, 256);
            SecretKey chatKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            // Calc for this user
            byte[] userChatSecret = new byte[256];
            for (int i = 0; i < 256; i++) {
                userChatSecret[i] = (byte) (chatSecret[i] - userSecret[i]);
            }
            sendPacket(new PacketData(PacketType.USER_CHAT_SECRET, userChatSecret));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}
