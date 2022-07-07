package com.gebb.wetell.server;

import com.gebb.wetell.connection.PacketType;
import com.gebb.wetell.dataclasses.PacketData;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class WeTellServer {
    public static final ResourceManager rm = new ResourceManager();

    protected static boolean running = false;

    private static WeTellServer server = null;
    private static RunArgumentsManager ram;

    private final HashMap<Long, ServerThread> threads = new HashMap<>();
    private final SQLManager sqlManager = new SQLManager("jdbc:sqlite:wetell.db");
    private final ServerSocket serverSocket;
    private final Queue<ServerThread> closeThreadQueue = new ConcurrentLinkedQueue<>();
    private final CountDownLatch latch = new CountDownLatch(1);
    private boolean isOverrideHash = false;
    private Thread idleThread;


    public static void main(String[] args) {
        ram = new RunArgumentsManager(args);
        try {
            server = new WeTellServer(24464);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ram.getOption("ssl")) {
            System.setProperty("javax.net.ssl.keyStore", rm.getInfo("sslCertPath"));
            System.setProperty("javax.net.ssl.keyStorePassword", rm.getInfo("sslPassword"));
        }
        System.out.println("Starting server...");
        WeTellServer.getInstance().idle();
    }

    private WeTellServer(int port) throws IOException {
        if (ram.getOption("ssl")) {
            serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(port);
        } else {
            serverSocket = new ServerSocket(port);
        }
    }

    private void idle() {
        running = true;
        System.out.println("Waiting for connection...");
        idleThread = new Thread(() -> {
            while (running) {
                try {
                    // Waiting to accept connection
                    Socket socket = serverSocket.accept();
                    if (running) {
                        ServerThread t = new ServerThread(socket);
                        threads.put(t.getId(), t);
                        System.out.println("Accepted connection");
                        // get last element and start it
                        t.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        idleThread.start();
        // Thread that kills threads from the stopQueue
        new Thread(() -> {
            while (running) {
                try {
                    latch.await();
                    // Wait for every Thread to close
                    Thread.sleep(10);
                    Iterator<ServerThread> it = closeThreadQueue.iterator();
                    while (it.hasNext()) {
                        threads.remove(it.next().getId());
                        it.remove();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // Time buffer so that the other thread starts before we quit
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startScanLoop();
        stop();
    }

    private void startScanLoop() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            String s = scanner.nextLine();
            if (Objects.equals(s, "q") || Objects.equals(s, "Q")) {
                running = false;
                System.out.println("Shutting down...");
                break;
            } else if (Objects.equals(s, "t") || Objects.equals(s, "T")) {
                sqlManager.createTables();
            } else if (Objects.equals(s, "r") || Objects.equals(s, "R")) {
                System.out.println("Number of ServerThreads active: " + threads.size());
            } else if (Objects.equals(s, "o") || Objects.equals(s, "O")) {
                isOverrideHash = !isOverrideHash;
                System.out.println("Override hashes " + (isOverrideHash ? "activated" : "deactivated"));
            }
        }
    }

    private void stop() {
        try {
            sqlManager.close();
            // Unblock wait for connection, kill that thread
            Socket s = new Socket();
            s.connect(new InetSocketAddress("localhost", serverSocket.getLocalPort()), 2000);
            idleThread.join();
            // Kill every server thread
            for (Map.Entry<Long, ServerThread> set : threads.entrySet()) {
                set.getValue().sendPacket(new PacketData(PacketType.CLOSE_CONNECTION));
                set.getValue().join();
            }
            // Kill the thread that waits for latch to count down and that kills the serverthreads in the stopqueue
            latch.countDown();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    protected void requestStopThread(long serverThreadID) {
        closeThreadQueue.add(threads.get(serverThreadID));
        latch.countDown();
    }

    public static WeTellServer getInstance() {
        return server;
    }

    protected SQLManager getSQLManager() {
        return sqlManager;
    }

    protected Collection<ServerThread> getThreads() {
        return threads.values();
    }

    protected boolean isOverrideHash() {
        return isOverrideHash;
    }
}
