package com.gebb.wetell.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WeTellServer {

    private ServerSocket serverSocket;
    private boolean running = true;

    public static void main(String[] args) {
        System.out.println("Starting server...");
        new WeTellServer().start(8000);
    }

    private void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        idle();
    }

    private void idle() {
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
