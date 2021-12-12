package hr.fer.tel.rassus.lab2.node.worker;

import hr.fer.tel.rassus.lab2.network.ConcurrentEmulatedSystemClock;
import hr.fer.tel.rassus.lab2.node.message.AckMessage;
import hr.fer.tel.rassus.lab2.node.message.DataMessage;
import hr.fer.tel.rassus.lab2.node.message.SocketMessage;
import hr.fer.tel.rassus.lab2.util.Pair;
import hr.fer.tel.rassus.lab2.util.Utils;
import hr.fer.tel.rassus.lab2.util.Vector;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiveWorker implements Runnable {

    private static final Logger logger = LogManager.getLogger(ReceiveWorker.class);
    private static final int RCV_BUF_SIZE = 1024;

    private final int nodeId;
    private final ConcurrentEmulatedSystemClock clock;
    private final Vector vectorTimestamp;
    private final DatagramSocket socket;
    private final AtomicBoolean running;
    private final BlockingQueue<DatagramPacket> sendQueue;
    private final Map<Integer, Pair<DatagramPacket, Long>> unAckPackets;
    private final Collection<DataMessage> tempMessages;
    private final byte[] rcvBuf;

    public ReceiveWorker(
            int nodeId,
            ConcurrentEmulatedSystemClock clock,
            Vector vectorTimestamp,
            DatagramSocket socket,
            AtomicBoolean running,
            BlockingQueue<DatagramPacket> sendQueue,
            Map<Integer, Pair<DatagramPacket, Long>> unAckPackets,
            Collection<DataMessage> tempMessages) {
        this.nodeId = nodeId;
        this.clock = clock;
        this.vectorTimestamp = vectorTimestamp;
        this.socket = socket;
        this.running = running;
        this.sendQueue = sendQueue;
        this.unAckPackets = unAckPackets;
        this.tempMessages = tempMessages;
        rcvBuf = new byte[RCV_BUF_SIZE];
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                DatagramPacket rcvPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
                socket.receive(rcvPacket);
                SocketMessage m = SocketMessage.deserialize(Utils.dataFromDatagramPacket(rcvPacket));
                // Update timestamps
                long newScalar = clock.currentTimeMillis(m.getScalarTimestamp());
                Vector newVector = vectorTimestamp.update(nodeId, m.getVectorTimestamp(), true);
                m.setScalarTimestamp(newScalar);
                m.setVectorTimestamp(newVector);
                switch (m.getType()) {
                    case DATA -> {
                        // Send ACK
                        SocketMessage ackMessage = new AckMessage(
                                nodeId,
                                newScalar,
                                newVector,
                                m.getMessageId());
                        sendQueue.put(Utils.createSendPacket(ackMessage, rcvPacket.getAddress(), rcvPacket.getPort()));
                        // Save message to temp collection
                        tempMessages.add((DataMessage) m);
                        vectorTimestamp.update(nodeId, null, false);
                    }
                    case ACK -> {
                        unAckPackets.remove(((AckMessage) m).getMessageIdToBeAck());
                        vectorTimestamp.update(nodeId, null, false);
                    }
                    default -> throw new IllegalArgumentException(
                            "'%s' is invalid SocketMessage type!".formatted(m.getType()));
                }
            } catch (SocketTimeoutException ignored) {
            } catch (Exception e) {
                logger.log(Level.FATAL, "", e);
                break;
            }
        }
    }

}
