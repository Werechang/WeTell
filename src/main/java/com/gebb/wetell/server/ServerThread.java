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
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class ServerThread extends Thread implements IConnectable {

    private final Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final KeyPair keyPair;
    private boolean isClientConnected = true;
    private PublicKey clientKey;
    private boolean clientHasKeyTransferred = false;
    private boolean clientReceivedKey = false;
    private Thread listenThread;


    protected ServerThread(@NotNull Socket clientSocket) {
        this.clientSocket = clientSocket;
        keyPair = KeyPairManager.generateRSAKeyPair();
        // connect
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            oos.flush();
            ois = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        startListenThread();
        try {
            sendPacket(new PacketData(PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(keyPair.getPublic())));
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
            case SUCCESS -> System.out.println("Success");
            case KEY -> {
                try {
                    clientKey = KeyPairManager.byteStreamToRSAPublicKey(data.getData());
                    clientHasKeyTransferred = true;
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }
            case LOGIN -> {
                if (!clientHasKeyTransferred) {
                    sendPacket(new PacketData(PacketType.KEYREQUEST));
                    return;
                }
                String[] unamepass = new String(data.getData(), StandardCharsets.UTF_8).split("\00");
                if (unamepass.length != 2) {
                    return;
                }
                hashString(unamepass[1]);
                }
            case KEY_TRANSFER_SUCCESS -> clientReceivedKey = true;
            default -> System.err.println("PacketType " + data.getType() + " is either corrupted or currently not supported. Data: " + new String(data.getData(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void sendPacket(PacketData data) {
        try {
            oos.writeObject(new Datapacket(clientKey, data.getType(), data.getData()));
            oos.flush();
            oos.reset();
        } catch (IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private byte[] hashString(String str) {
        byte[] pepper = {32, -23, -45, -67, 92, -66, 100, -91, 80, -122, -51, 42, -21, 116, 17, -42};
        KeySpec spec = new PBEKeySpec(str.toCharArray(), pepper, 65536, 128);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        throw new UnknownError("An error occurred while hashing a string");
    }
}
