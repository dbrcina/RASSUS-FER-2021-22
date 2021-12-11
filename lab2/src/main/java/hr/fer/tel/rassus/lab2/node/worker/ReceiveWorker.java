package hr.fer.tel.rassus.lab2.node.worker;

import hr.fer.tel.rassus.lab2.node.message.AckMessage;
import hr.fer.tel.rassus.lab2.node.message.SocketMessage;
import hr.fer.tel.rassus.lab2.util.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReceiveWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(ReceiveWorker.class.getName());
    private static final int RCV_BUF_SIZE = 1024;

    private final int nodeId;
    private final DatagramSocket socket;
    private final AtomicBoolean running;
    private final Queue<AckMessage> ackRcvQueue;
    private final byte[] rcvBuf;

    public ReceiveWorker(int nodeId, DatagramSocket socket, AtomicBoolean running, Queue<AckMessage> ackRcvQueue) {
        this.nodeId = nodeId;
        this.socket = socket;
        this.running = running;
        this.ackRcvQueue = ackRcvQueue;
        rcvBuf = new byte[RCV_BUF_SIZE];
    }

    @Override
    public void run() {
        DatagramPacket rcvPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
        while (running.get()) {
            try {
                socket.receive(rcvPacket);
                SocketMessage m = SocketMessage.deserialize(Utils.dataFromDatagramPacket(rcvPacket));
                switch (m.getType()) {
                    case ACK -> ackRcvQueue.add((AckMessage) m);
                    case DATA -> {
                        byte[] ackBuf = SocketMessage.serialize(new AckMessage(nodeId));
                        InetAddress address = rcvPacket.getAddress();
                        int port = rcvPacket.getPort();
                        DatagramPacket ackPacket = new DatagramPacket(ackBuf, ackBuf.length, address, port);
                        socket.send(ackPacket);
                    }
                    default -> logger.warning("'%s' is invalid SocketMessage type!".formatted(m.getType()));
                }
            } catch (SocketTimeoutException ignored) {
            } catch (IOException | ClassNotFoundException e) {
                logger.log(Level.SEVERE, "", e);
                break;
            }
        }
    }

}
