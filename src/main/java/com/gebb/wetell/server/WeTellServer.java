package com.gebb.wetell.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class WeTellServer extends ServerSocket {

    private boolean running = false;
    private final ArrayList<ServerThread> threads = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Starting server...");
        try {
            new WeTellServer(21394).idle();
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
                    // Waiting to accept connection
                    Socket socket = this.accept();
                    threads.add(new ServerThread(socket));
                    System.out.println("Accepted connection");
                    // get last element and start it
                    threads.get(threads.size()-1).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        idleThread.start();

        try {
            idleThread.join();
            for (ServerThread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
