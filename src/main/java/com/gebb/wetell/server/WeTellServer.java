package com.gebb.wetell.server;

import com.gebb.wetell.PacketData;
import com.gebb.wetell.PacketType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class WeTellServer extends ServerSocket {

    protected static boolean running = false;
    private final HashMap<Long, ServerThread> threads = new HashMap<>();
    private static WeTellServer server = null;
    private final SQLManager sqlManager = new SQLManager("jdbc:sqlite:wetell.db");
    private Thread idleThread;

    private final Queue<ServerThread> closeThreadQueue = new ConcurrentLinkedQueue<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            server = new WeTellServer(80);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Starting server...");
        WeTellServer.getInstance().idle();
    }

    private WeTellServer(int port) throws IOException {
        super(port);
    }

    private void idle() {
        running = true;
        System.out.println("Waiting for connection...");
        idleThread = new Thread(() -> {
            while (running) {
                try {
                    // Waiting to accept connection
                    Socket socket = this.accept();
                    ServerThread t = new ServerThread(socket);
                    threads.put(t.getId(), t);
                    System.out.println("Accepted connection");
                    // get last element and start it
                    t.start();
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
        while (true) {
            String s = scanner.nextLine();
            if (Objects.equals(s, "q") || Objects.equals(s, "Q")) {
                running = false;
                System.out.println("Shutting down...");
                break;
            } else if (Objects.equals(s, "t") || Objects.equals(s, "T")) {
                sqlManager.createTables();
            } else if (Objects.equals(s, "r") || Objects.equals(s, "R")) {
                System.out.println("Number of ServerThreads active: " + threads.size());
            }
        }
    }

    private void stop() {
        try {
            sqlManager.close();
            //Socket s = new Socket();
            //s.connect(new InetSocketAddress("localhost", this.getLocalPort()));
            idleThread.join();
            for (Map.Entry<Long, ServerThread> set: threads.entrySet()) {
                set.getValue().sendPacket(new PacketData(PacketType.CLOSE_CONNECTION));
                set.getValue().join();
            }
        } catch (InterruptedException e) {
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
}
