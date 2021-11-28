package com.gebb.wetell.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class WeTellServer extends ServerSocket {

    protected static boolean running = false;
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
        // Time buffer so that the other thread starts before we quit
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String s = scanner.nextLine();
            if (Objects.equals(s, "q") || Objects.equals(s, "Q")) {
                running = false;
                System.out.println("Shutting down...");
                break;
            }
        }
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress("localhost", this.getLocalPort()));
            idleThread.join();
            for (ServerThread t : threads) {
                t.join();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
