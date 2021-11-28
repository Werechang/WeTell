package com.gebb.wetell.server;

import com.gebb.wetell.IConnectable;
import com.gebb.wetell.Datapacket;
import com.gebb.wetell.KeyPairManager;
import com.gebb.wetell.PacketData;
import com.gebb.wetell.client.WeTellClient;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;

public class ServerThread extends Thread implements IConnectable {

    private final Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final KeyPair keyPair;
    private boolean isClientConnected = true;
    private PublicKey clientKey;
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
        // TODO send key, request key
        try {
            listenThread.join();
            oos.flush();
            oos.close();
            ois.close();
            clientSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startListenThread() {
        listenThread = new Thread(() -> {
            while (isClientConnected && WeTellServer.running) {
                try {
                    Datapacket packet = (Datapacket) ois.readObject();
                    if (packet != null) {
                        execPacket(packet.getPacketData(null));
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
                } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InterruptedException e) {
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
        // TODO Implement actions
        switch (data.getType()) {
            case MSG -> System.out.println(new String(data.getData(), StandardCharsets.UTF_8));
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
}
