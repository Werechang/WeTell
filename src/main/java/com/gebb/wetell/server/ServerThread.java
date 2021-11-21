package com.gebb.wetell.server;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {

    private final Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    protected ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        connect();
    }

    private void connect() {
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

    }
}
