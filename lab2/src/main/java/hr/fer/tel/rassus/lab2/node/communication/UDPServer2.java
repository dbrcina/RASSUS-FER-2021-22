package hr.fer.tel.rassus.lab2.node.communication;

import hr.fer.tel.rassus.lab2.network.SimpleSimulatedDatagramSocket;
import hr.fer.tel.rassus.lab2.node.communication.messages.AckMessage;
import hr.fer.tel.rassus.lab2.node.communication.messages.DataMessage;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UDPServer2 implements Runnable {

    private static final Logger logger = Logger.getLogger(UDPServer2.class.getName());
    private static final int N_THREADS = 2;
    private static final int RCV_BUF_SIZE = 256;
    private static final int SO_TIMEOUT = 10;

    private final int port;
    private final AtomicBoolean running;
    private final ExecutorService threadPool;
    private final byte[] rcvBuf;
    private final Collection<Integer> receivedPacketIds;

    public UDPServer2(int port, AtomicBoolean running) {
        this.port = port;
        this.running = running;
        threadPool = Executors.newFixedThreadPool(N_THREADS);
        rcvBuf = new byte[RCV_BUF_SIZE];
        receivedPacketIds = new HashSet<>();
    }

    @Override
    public void run() {
        try (DatagramSocket serverSocket = new SimpleSimulatedDatagramSocket(port, 0.3, 1000)) {
            serverSocket.setSoTimeout(SO_TIMEOUT);
            while (running.get()) {
                DatagramPacket rcvPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
                try {
                    serverSocket.receive(rcvPacket);
                    threadPool.submit(new ReceivedPacketHandler(serverSocket, rcvPacket, receivedPacketIds));
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "", e);
        } finally {
            threadPool.shutdown();
        }
    }

    private record ReceivedPacketHandler(
            DatagramSocket serverSocket,
            DatagramPacket p,
            Collection<Integer> receivedPacketIds) implements Runnable {
        @Override
        public void run() {
            try {
                DatagramPacket ackPacket = AckMessage.getInstance().getAckPacket();
                ackPacket.setAddress(p.getAddress());
                ackPacket.setPort(p.getPort());
                serverSocket.send(ackPacket);
                try (ByteArrayInputStream bis = new ByteArrayInputStream(p.getData(), p.getOffset(), p.getLength());
                     ObjectInputStream objis = new ObjectInputStream(bis)) {
                    DataMessage dataMessage = (DataMessage) objis.readObject();
                    if (receivedPacketIds.contains(dataMessage.id())) {
                        return;
                    }
                    receivedPacketIds.add(dataMessage.id());
                    // store received data message
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "", e);
            }
        }
    }

}
