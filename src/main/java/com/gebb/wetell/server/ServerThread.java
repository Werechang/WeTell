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
        sendKey();
        close();
    }

    private void close() {
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
            case KEYREQUEST -> sendKey();
            case KEY -> {
                try {
                    clientKey = KeyPairManager.byteStreamToRSAPublicKey(data.getData());
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                sendPacket(new PacketData(PacketType.KEY_TRANSFER_SUCCESS));
            }
            case LOGIN -> login(data, false);
            case SIGNIN -> login(data, true);
            case KEY_TRANSFER_SUCCESS -> clientReceivedKey = true;
            case MSG -> addMessage(data.getData());
            case LOGOUT -> {
                userId = -1;
                sendPacket(new PacketData(PacketType.LOGOUT));
            }
            case FETCH_CHATS -> fetchChats();
            case FETCH_MSGS -> fetchMessages(data.getData());
            case ADD_CHAT -> addChat(data.getData());
            case ADD_USER_TO_CHAT -> addUserToChat(data.getData());
            case CLOSE_CONNECTION -> close();
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
        System.out.println("Sent packet: " + data.getType());
        if (data.getType() == PacketType.ERROR) {
            System.out.println("Error");
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
        if (userId == -1) {
            if (clientKey != null) {
                // [0] username | [1] password
                String[] usernamePassword = new String(data.getData(), StandardCharsets.UTF_8).split("\00");
                if (usernamePassword.length == 2) {
                    if (usernamePassword[0].length() >= 4 || usernamePassword[1].length() >= 4) {
                        if (isFirstLogin) {
                            String salt = generateSalt();
                            try {
                                userId = WeTellServer.getInstance().getSQLManager().addUser(usernamePassword[0], hashString(usernamePassword[1]+salt), salt);
                            } catch (NullPointerException e) {
                                sendPacket(new PacketData(PacketType.ERROR, "Username already exists.".getBytes(StandardCharsets.UTF_8)));
                            }
                            sendPacket(new PacketData(PacketType.LOGIN_SUCCESS, usernamePassword[0].getBytes(StandardCharsets.UTF_8)));
                            ByteBuffer buffer = ByteBuffer.allocate(4);
                            buffer.putInt(userId);
                            sendPacket(new PacketData(PacketType.USER_ID, buffer.array()));
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
                    } else {
                        sendPacket(new PacketData(PacketType.ERROR, "Username or password length must be at least 4.".getBytes(StandardCharsets.UTF_8)));
                    }
                } else {
                    sendPacket(new PacketData(PacketType.ERROR, "The login data is corrupted. Try again.".getBytes(StandardCharsets.UTF_8)));
                }
            } else {
                sendPacket(new PacketData(PacketType.KEYREQUEST), false);
            }
        } else {
            sendPacket(new PacketData(PacketType.ERROR, "A user is currently logged in.".getBytes(StandardCharsets.UTF_8)));
        }

    }

    @Override
    public boolean isLoggedInAndSecureConnection() {
        if (userId == -1) {
            sendPacket(new PacketData(PacketType.ERROR, "You are not logged in".getBytes(StandardCharsets.UTF_8)));
            return false;
        }
        if (!clientReceivedKey) {
            sendKey();
            return false;
        }
        if (clientKey == null) {
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

    private void addChat(byte[] data) {
        if (isLoggedInAndSecureConnection()) {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            try {
                ObjectInputStream is = new ObjectInputStream(bis);
                is.setObjectInputFilter(new DatapacketFilter());
                Object o = is.readObject();
                // Instanceof checks to stay safe
                if (o instanceof ChatData) {
                    ChatData chatData = (ChatData) o;
                    if (chatData.getName() == null) {
                        sendPacket(new PacketData(PacketType.ERROR, "Chat name must not be null".getBytes(StandardCharsets.UTF_8)));
                    } else {
                        int chatId;
                        try {
                            chatId = WeTellServer.getInstance().getSQLManager().addChat(chatData.getName());
                        } catch (NullPointerException e) {
                            sendPacket(new PacketData(PacketType.ERROR, "Something went wrong while creating a new chat.".getBytes(StandardCharsets.UTF_8)));
                            return;
                        }
                        addUserToChat(new ContactData(chatId, userId));
                        fetchChatData(chatId, chatData.getName());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void addUserToChat(byte[] data) {
        if (isLoggedInAndSecureConnection()) {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            try {
                ObjectInputStream is = new ObjectInputStream(bis);
                is.setObjectInputFilter(new DatapacketFilter());
                Object o = is.readObject();
                // Instanceof checks to stay safe
                if (o instanceof ContactData) {
                    ContactData contactData = (ContactData) o;
                    if (contactData.getChatId() == -1) {
                        sendPacket(new PacketData(PacketType.ERROR, "Chat not specified.".getBytes(StandardCharsets.UTF_8)));
                    } else if (contactData.getUserId() == -1) {
                        sendPacket(new PacketData(PacketType.ERROR, "User not specified.".getBytes(StandardCharsets.UTF_8)));
                    } else if (contactData.getUserId() == userId) {
                        sendPacket(new PacketData(PacketType.ERROR, "User is this user.".getBytes(StandardCharsets.UTF_8)));
                    } else {
                        try {
                            WeTellServer.getInstance().getSQLManager().addContact(contactData.getUserId(), contactData.getChatId());
                            fetchChatDataForUser(contactData.getChatId(), contactData.getUserId());
                        } catch (NullPointerException e) {
                            sendPacket(new PacketData(PacketType.ERROR, "User is already in this chat.".getBytes(StandardCharsets.UTF_8)));
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void addUserToChat(ContactData contactData) {
        if (isLoggedInAndSecureConnection()) {
            try {
                WeTellServer.getInstance().getSQLManager().addContact(contactData.getUserId(), contactData.getChatId());
            } catch (NullPointerException e) {
                sendPacket(new PacketData(PacketType.ERROR, "User is already in this chat.".getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    private void addMessage(byte[] data) {
        if (isLoggedInAndSecureConnection()) {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            try {
                ObjectInputStream is = new ObjectInputStream(bis);
                is.setObjectInputFilter(new DatapacketFilter());
                Object o = is.readObject();
                // Instanceof checks to stay safe
                if (o instanceof MessageData) {
                    MessageData messageData = (MessageData) o;
                    if (messageData.getChatId() == -1) {
                        sendPacket(new PacketData(PacketType.ERROR, "Chat not specified.".getBytes(StandardCharsets.UTF_8)));
                    } else {
                        try {
                            String time = WeTellServer.getInstance().getSQLManager().newMessage(userId, messageData.getChatId(), messageData.getMsgContent());
                            sendMessageToChat(new MessageData(userId, messageData.getChatId(), messageData.getMsgContent(), time));
                        } catch (NullPointerException e) {
                            sendPacket(new PacketData(PacketType.ERROR, "User is not in the chat.".getBytes(StandardCharsets.UTF_8)));
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchChats() {
        if (isLoggedInAndSecureConnection()) {
            try {
                ArrayList<ChatData> chatData = WeTellServer.getInstance().getSQLManager().fetchChatsForUser(userId);
                if (chatData.size() != 0) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        ObjectOutputStream chatOS = new ObjectOutputStream(bos);
                        chatOS.writeObject(chatData);
                        chatOS.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendPacket(new PacketData(PacketType.FETCH_CHATS, bos.toByteArray()));
                }
            } catch (NullPointerException e) {
                sendPacket(new PacketData(PacketType.ERROR, "An error occurred while reading the chat data.".getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    private void fetchMessages(byte[] data) {
        if (isLoggedInAndSecureConnection()) {
            ByteBuffer chatIdWrapped = ByteBuffer.wrap(data);
            ArrayList<MessageData> messageData = WeTellServer.getInstance().getSQLManager().fetchMessagesForChat(chatIdWrapped.getInt());
            if (messageData.size() != 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream msgOS = new ObjectOutputStream(bos);
                    msgOS.writeObject(messageData);
                    msgOS.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendPacket(new PacketData(PacketType.FETCH_MSGS, bos.toByteArray()));
            }
        }
    }

    private void fetchChatDataForUser(int chatId, int userId) {
        for (ServerThread thread : WeTellServer.getInstance().getThreads()) {
            if (thread.userId == userId) {
                thread.fetchChatData(chatId);
            }
        }
    }

    private void fetchChatData(int chatId) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(new ChatData(WeTellServer.getInstance().getSQLManager().getChatName(chatId), chatId));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendPacket(new PacketData(PacketType.FETCH_CHAT, bos.toByteArray()));
    }

    private void fetchChatData(int chatId, String chatName) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(new ChatData(chatName, chatId));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendPacket(new PacketData(PacketType.FETCH_CHAT, bos.toByteArray()));
    }

    private void sendMessageToChat(MessageData messageData) {
        try {
            ArrayList<Integer> userIds = WeTellServer.getInstance().getSQLManager().getUsersInChat(messageData.getChatId());
            for (ServerThread thread : WeTellServer.getInstance().getThreads()) {
                if (userIds.contains(thread.userId)) {
                    fetchMessageData(messageData);
                }
            }
        } catch (NullPointerException e) {
            // Case: there are no users in the chat
            e.printStackTrace();
        }
    }

    private void fetchMessageData(MessageData messageData) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(messageData);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendPacket(new PacketData(PacketType.FETCH_MESSAGE, bos.toByteArray()));
    }
}
