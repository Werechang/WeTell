package com.gebb.wetell.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WeTellServer extends ServerSocket {

    private ServerSocket serverSocket;
    private boolean running = false;

    public static void main(String[] args) {
        System.out.println("Starting server...");
        try {
            new WeTellServer(8000).idle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WeTellServer(int port) throws IOException {
        super(port);
    }

    private void idle() {
        running = true;
        System.out.println("Waiting for connection...");
        Thread idleThread = new Thread(() -> {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    new ServerThread(socket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        idleThread.start();
        try {
            idleThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping server...");
    }
}
