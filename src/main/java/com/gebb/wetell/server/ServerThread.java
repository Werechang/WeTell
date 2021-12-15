package com.gebb.wetell.server;

import com.gebb.wetell.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;

public class ServerThread extends Thread implements IConnectable {

    private final Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final KeyPair keyPair;
    private boolean isClientConnected = true;
    private PublicKey clientKey = null;
    private boolean clientHasKeyTransferred = false;
    private boolean clientReceivedKey = false;
    private Thread listenThread;
    private int userId = -1;


    protected ServerThread(@NotNull Socket clientSocket) {
        this.clientSocket = clientSocket;
        keyPair = KeyPairManager.generateRSAKeyPair();
        // connect
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            oos.flush();
            ois = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            ois.setObjectInputFilter(new DatapacketFilter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        startListenThread();
        try {
            sendPacket(new PacketData(PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(keyPair.getPublic())), false);
        } catch (NoSuchAlgorithmException e) {
            sendPacket(new PacketData(PacketType.CLOSE_CONNECTION));
            e.printStackTrace();
        }
        try {
            listenThread.join();
            oos.flush();
            oos.close();
            ois.close();
            clientSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        WeTellServer.getInstance().requestStopThread(this.getId());
    }

    private void startListenThread() {
        listenThread = new Thread(() -> {
            while (isClientConnected && WeTellServer.running) {
                try {
                    Datapacket packet = (Datapacket) ois.readObject();
                    if (packet != null) {
                        execPacket(packet.getPacketData(clientReceivedKey ? keyPair.getPrivate() : null));
                    }
                    Thread.sleep(10);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    // Check if connection got closed
                    if (e.getClass().getName().equals("java.io.EOFException")) {
                        isClientConnected = false;
                    }
                    if (e.getMessage().equals("Connection reset")) {
                        isClientConnected = false;
                    }
                } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InterruptedException | InvalidSignatureException e) {
                    e.printStackTrace();
                }
            }
        });
        listenThread.start();
    }

    @Override
    public void execPacket(PacketData data) {
        if (data == null) {
            return;
        }
        switch (data.getType()) {
            case KEYREQUEST -> {
                try {
                    sendPacket(new PacketData(PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(keyPair.getPublic())));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            case KEY -> {
                try {
                    clientKey = KeyPairManager.byteStreamToRSAPublicKey(data.getData());
                    clientHasKeyTransferred = true;
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                sendPacket(new PacketData(PacketType.KEY_TRANSFER_SUCCESS));
            }
            case LOGIN -> login(data, false);
            case SIGNIN -> login(data, true);
            case KEY_TRANSFER_SUCCESS -> clientReceivedKey = true;
            case MSG -> {
                if (userId != -1) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(data.getData());
                    try {
                        ObjectInputStream is = new ObjectInputStream(bis);
                        is.setObjectInputFilter(new DatapacketFilter());
                        Object o = is.readObject();
                        // Instanceof checks to stay safe
                        if (o instanceof MessageData) {
                            MessageData messageData = (MessageData) o;
                            if (messageData.getChatId() == -1) {
                                sendPacket(new PacketData(PacketType.ERROR, "Chat not specified.".getBytes(StandardCharsets.UTF_8)));
                            }
                            try {
                                WeTellServer.getInstance().getSQLManager().newMessage(userId, messageData.getChatId(), messageData.getMsgContent());
                            } catch (NullPointerException e) {
                                sendPacket(new PacketData(PacketType.ERROR, "User is not in the chat.".getBytes(StandardCharsets.UTF_8)));
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    sendPacket(new PacketData(PacketType.ERROR, "You are not logged in".getBytes(StandardCharsets.UTF_8)));
                }
            }
            case LOGOUT -> userId = -1;
            case FETCH_CHATS -> {
                if (userId != -1 && clientReceivedKey) {
                    ArrayList<ChatData> chatData = WeTellServer.getInstance().getSQLManager().fetchChatsForUser(userId);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        ObjectOutputStream msgOS = new ObjectOutputStream(bos);
                        msgOS.writeObject(chatData);
                        msgOS.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendPacket(new PacketData(PacketType.FETCH_CHATS, bos.toByteArray()));
                }
            }
            case FETCH_MSGS -> {
                if (userId != -1 && clientReceivedKey) {
                    ByteBuffer chatIdWrapped = ByteBuffer.wrap(data.getData());
                    ArrayList<MessageData> messageData = WeTellServer.getInstance().getSQLManager().fetchMessagesForChat(chatIdWrapped.getInt());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        ObjectOutputStream msgOS = new ObjectOutputStream(bos);
                        msgOS.writeObject(messageData);
                        msgOS.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendPacket(new PacketData(PacketType.FETCH_MSGS, bos.toByteArray()));
                } else {
                    sendPacket(new PacketData(PacketType.ERROR, "You are not logged in".getBytes(StandardCharsets.UTF_8)));
                }
            }
            case ADD_CHAT -> {
                if (userId != -1) {
                    ByteBuffer userOtherIdWrapped = ByteBuffer.wrap(data.getData());
                    int userOtherId = userOtherIdWrapped.getInt();
                    try {
                        String chatName = WeTellServer.getInstance().getSQLManager().getUsername(userOtherId);
                        int chatId = WeTellServer.getInstance().getSQLManager().addChat(chatName);
                        WeTellServer.getInstance().getSQLManager().addContact(userOtherId, chatId);
                        WeTellServer.getInstance().getSQLManager().addContact(userId, chatId);

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {
                            ObjectOutputStream os = new ObjectOutputStream(bos);
                            os.writeObject(new ChatData(chatName, chatId));
                            os.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        sendPacket(new PacketData(PacketType.ADD_CHAT, bos.toByteArray()));
                        // TODO Fetch chats for other user
                    } catch (NullPointerException e) {
                        sendPacket(new PacketData(PacketType.ERROR, "User does not exist.".getBytes(StandardCharsets.UTF_8)));
                    }
                } else {
                    sendPacket(new PacketData(PacketType.ERROR, "You are not logged in".getBytes(StandardCharsets.UTF_8)));
                }
            }
            case FETCH_USERNAME -> {
                if (userId != -1 && clientReceivedKey) {
                    ByteBuffer userIdWrapped = ByteBuffer.wrap(data.getData());
                    sendPacket(new PacketData(PacketType.FETCH_USERNAME, WeTellServer.getInstance().getSQLManager().getUsername(userIdWrapped.getInt()).getBytes(StandardCharsets.UTF_8)));
                } else {
                    sendPacket(new PacketData(PacketType.ERROR, "You are not logged in".getBytes(StandardCharsets.UTF_8)));
                }
            }
            default -> System.err.println("PacketType " + data.getType() + " is either corrupted or currently not supported. Data: " + new String(data.getData(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void sendPacket(PacketData data) {
        sendPacket(data, clientReceivedKey);
    }

    @Override
    public void sendPacket(PacketData data, boolean isEncrypted) {
        try {
            oos.writeObject(new Datapacket(clientKey, data.getType(), isEncrypted, data.getData()));
            oos.flush();
            oos.reset();
        } catch (IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private String hashString(String str) {
        byte[] pepper = {32, -23, -45, -67, 92, -66, 100, -91, 80, -122, -51, 42, -21, 116, 17, -42};
        KeySpec spec = new PBEKeySpec(str.toCharArray(), pepper, 65536, 128);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return new String(factory.generateSecret(spec).getEncoded(), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        throw new UnknownError("An error occurred while hashing a string");
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] a = new byte[16];
        random.nextBytes(a);
        return new String(a, StandardCharsets.UTF_8);
    }

    // isFirstLogin means signIn
    private void login(PacketData data, boolean isFirstLogin) {
        if (userId != -1) {
            sendPacket(new PacketData(PacketType.ERROR, "A user is currently logged in.".getBytes(StandardCharsets.UTF_8)));
            return;
        }
        if (!clientHasKeyTransferred) {
            sendPacket(new PacketData(PacketType.KEYREQUEST), false);
            return;
        }
        String[] usernamePassword = new String(data.getData(), StandardCharsets.UTF_8).split("\00");
        if (usernamePassword.length != 2) {
            sendPacket(new PacketData(PacketType.ERROR, "The login data is corrupted. Try again.".getBytes(StandardCharsets.UTF_8)));
            return;
        }
        if (usernamePassword[0].length() < 4 || usernamePassword[1].length() < 4) {
            sendPacket(new PacketData(PacketType.ERROR, "Username or password length must be at least 4.".getBytes(StandardCharsets.UTF_8)));
            return;
        }
        if (isFirstLogin) {
            String salt = generateSalt();
            try {
                userId = WeTellServer.getInstance().getSQLManager().addUser(usernamePassword[0], hashString(usernamePassword[1]+salt), salt);
            } catch (NullPointerException e) {
                sendPacket(new PacketData(PacketType.ERROR, "Username already exists.".getBytes(StandardCharsets.UTF_8)));
            }
            sendPacket(new PacketData(PacketType.LOGIN_SUCCESS, usernamePassword[0].getBytes(StandardCharsets.UTF_8)));
        } else {
            try {
                SQLManager.UserData ud = WeTellServer.getInstance().getSQLManager().getUserData(usernamePassword[0]);
                // If hashes match
                if (ud.getHashedPassword().equals(hashString(usernamePassword[1]+ud.getSalt()))) {
                    userId = WeTellServer.getInstance().getSQLManager().getUserId(usernamePassword[0]);
                    sendPacket(new PacketData(PacketType.LOGIN_SUCCESS, usernamePassword[0].getBytes(StandardCharsets.UTF_8)));
                }
            } catch (NullPointerException ignored) {
                sendPacket(new PacketData(PacketType.ERROR, "Username or password is wrong.".getBytes(StandardCharsets.UTF_8)));
            }
        }
    }
}
