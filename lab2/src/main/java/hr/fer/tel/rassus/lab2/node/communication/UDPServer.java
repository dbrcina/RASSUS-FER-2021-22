package hr.fer.tel.rassus.lab2.node.communication;

import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;
import hr.fer.tel.rassus.lab2.network.SimpleSimulatedDatagramSocket;
import hr.fer.tel.rassus.lab2.node.communication.messages.AckMessage;
import hr.fer.tel.rassus.lab2.node.communication.messages.DataMessage;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public final class UDPServer implements Runnable {

    private static final int N_THREADS = 2;

    private static final Logger logger = Logger.getLogger(UDPServer.class.getName());

    private final int port;
    private final EmulatedSystemClock clock;
    private final AtomicBoolean running;
    private final AtomicLong scalarValue;
    private final AtomicInteger activeConnections;
    private final ExecutorService executorService;
    private final byte[] rcvBuf;
    private final Map<Integer, Integer> seqMap;
    private DatagramSocket serverSocket;

    public UDPServer(int port, EmulatedSystemClock clock, AtomicBoolean running, AtomicLong scalarValue) {
        this.port = port;
        this.clock = clock;
        this.running = running;
        this.scalarValue = scalarValue;
        activeConnections = new AtomicInteger();
        executorService = Executors.newFixedThreadPool(N_THREADS);
        rcvBuf = new byte[256];
        seqMap = new HashMap<>();
    }

    public void startup() {
        try {
            serverSocket = new SimpleSimulatedDatagramSocket(port, 0.3, 1000);
        } catch (SocketException e) {
            logger.severe("Server socket creation: " + e.getMessage());
            System.exit(-1);
        }
    }

    public void loop() {
        while (running.get()) {
            // Receive packet.
            DatagramPacket rcvPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
            try {
                serverSocket.receive(rcvPacket);
            } catch (Exception e) {
                logger.severe("receive(): " + e.getMessage());
                break;
            }
            // Send ACK and handle receive packet in another thread.
            try {
                executorService.execute(() -> handlePacket(rcvPacket));
                activeConnections.getAndIncrement();
            } catch (Exception e) {
                logger.severe("execute(): " + e.getMessage());
                break;
            }
        }
    }

    private void handlePacket(DatagramPacket p) {
        DatagramPacket ackPacket = AckMessage.getAckPacket();
        ackPacket.setAddress(p.getAddress());
        ackPacket.setPort(p.getPort());
        try {
            serverSocket.send(ackPacket);
            try (ObjectInputStream os = new ObjectInputStream(
                    new ByteArrayInputStream(p.getData(), p.getOffset(), p.getLength()))) {
                DataMessage data = (DataMessage) os.readObject();
                Integer prevSeq = seqMap.get(data.nodeId());
                if (prevSeq != null && prevSeq <= data.seq()) {
                    System.out.println(data.nodeId() + " dobio sam isti: " + data.seq());
                    return;
                }
                seqMap.put(data.nodeId(), data.seq());
                System.out.println(data.nodeId() + " dobio sam novi: " + data.seq());
            }
        } catch (Exception e) {
            logger.severe("handlePacket(): " + e.getMessage());
        } finally {
            activeConnections.getAndDecrement();
        }
    }

    public void shutdown() {
        while (activeConnections.get() > 0) {
            logger.warning("There are still active connections...waiting before shutdown!");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        if (activeConnections.get() <= 0) {
            logger.info("Starting server shutdown...");
            try {
                serverSocket.close();
            } catch (Exception e) {
                logger.severe(e.getMessage());
            } finally {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                }
            }
            logger.info("Server has been shutdown!");
        }
    }

    @Override
    public void run() {
        startup();
        loop();
    }

}
