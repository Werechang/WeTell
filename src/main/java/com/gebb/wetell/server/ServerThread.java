package com.gebb.wetell.server;

import com.gebb.wetell.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

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
    private String username = null;


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
                    Thread.sleep(2000);
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
            case KEY -> {
                try {
                    clientKey = KeyPairManager.byteStreamToRSAPublicKey(data.getData());
                    clientHasKeyTransferred = true;
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                sendPacket(new PacketData(PacketType.KEY_TRANSFER_SUCCESS));
            }
            case LOGIN -> {
                if (!clientHasKeyTransferred) {
                    sendPacket(new PacketData(PacketType.KEYREQUEST), false);
                    return;
                }
                String[] usernamePassword = new String(data.getData(), StandardCharsets.UTF_8).split("\00");
                if (usernamePassword.length != 2) {
                    return;
                }
                try {
                    SQLManager.UserData ud = WeTellServer.getInstance().getSQLManager().getUser(usernamePassword[0]);
                    // If hashes do not match
                    if (!ud.getHashedPassword().equals(hashString(usernamePassword[1]+ud.getSalt()))) {
                        throw new NullPointerException();
                    }
                    username = usernamePassword[0];
                } catch (NullPointerException e) {
                    sendPacket(new PacketData(PacketType.ERROR, "Username or password is wrong.".getBytes(StandardCharsets.UTF_8)));
                }
                }
            case SIGNIN -> {
                if (!clientHasKeyTransferred) {
                    sendPacket(new PacketData(PacketType.KEYREQUEST), false);
                    return;
                }
                String[] usernamePassword = new String(data.getData(), StandardCharsets.UTF_8).split("\00");
                if (usernamePassword.length != 2) {
                    return;
                }
                if (usernamePassword[0].length() < 4 || usernamePassword[1].length() < 4) {
                    sendPacket(new PacketData(PacketType.ERROR, "Username or password length must be at least 4.".getBytes(StandardCharsets.UTF_8)));
                    return;
                }
                String salt = generateSalt();
                try {
                    WeTellServer.getInstance().getSQLManager().addUser(usernamePassword[0], hashString(usernamePassword[1]+salt), salt);
                } catch (NullPointerException e) {
                    sendPacket(new PacketData(PacketType.ERROR, "Username already exists.".getBytes(StandardCharsets.UTF_8)));
                }
                username = usernamePassword[0];
                sendPacket(new PacketData(PacketType.LOGIN_SUCCESS));
            }
            case KEY_TRANSFER_SUCCESS -> clientReceivedKey = true;
            case MSG -> {
                if (username == null) {
                    sendPacket(new PacketData(PacketType.ERROR, "You are not logged in".getBytes(StandardCharsets.UTF_8)));
                    return;
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
}
