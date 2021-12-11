package hr.fer.tel.rassus.lab2.node.worker;

import hr.fer.tel.rassus.lab2.network.ConcurrentEmulatedSystemClock;
import hr.fer.tel.rassus.lab2.node.message.AckMessage;
import hr.fer.tel.rassus.lab2.node.message.SocketMessage;
import hr.fer.tel.rassus.lab2.util.Pair;
import hr.fer.tel.rassus.lab2.util.Utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(ReceiveWorker.class.getName());
    private static final int RCV_BUF_SIZE = 1024;

    private final int nodeId;
    private final ConcurrentEmulatedSystemClock clock;
    private final DatagramSocket socket;
    private final AtomicBoolean running;
    private final BlockingQueue<DatagramPacket> sendQueue;
    private final Map<Integer, Pair<DatagramPacket, Long>> unAckPackets;
    private final Consumer<SocketMessage> messageCollectionsUpdater;
    private final byte[] rcvBuf;

    public ReceiveWorker(
            int nodeId,
            ConcurrentEmulatedSystemClock clock,
            DatagramSocket socket,
            AtomicBoolean running,
            BlockingQueue<DatagramPacket> sendQueue,
            Map<Integer, Pair<DatagramPacket, Long>> unAckPackets,
            Consumer<SocketMessage> messageCollectionsUpdater) {
        this.nodeId = nodeId;
        this.clock = clock;
        this.socket = socket;
        this.running = running;
        this.sendQueue = sendQueue;
        this.unAckPackets = unAckPackets;
        this.messageCollectionsUpdater = messageCollectionsUpdater;
        rcvBuf = new byte[RCV_BUF_SIZE];
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                DatagramPacket rcvPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
                socket.receive(rcvPacket);
                SocketMessage m = SocketMessage.deserialize(Utils.dataFromDatagramPacket(rcvPacket));
                // Update scalar timestamp
                long scalarTimestamp = clock.currentTimeMillis(m.getScalarTimestamp());
                m.setScalarTimestamp(scalarTimestamp);
                switch (m.getType()) {
                    case DATA -> {
                        // Send ACK
                        SocketMessage ackMessage = new AckMessage(nodeId, scalarTimestamp, m.getMessageId());
                        sendQueue.put(Utils.createSendPacket(ackMessage, rcvPacket.getAddress(), rcvPacket.getPort()));
                        // Save message to temp collection
                        messageCollectionsUpdater.accept(m);
                    }
                    case ACK -> unAckPackets.remove(((AckMessage) m).getMessageIdToBeAck());
                    default -> throw new IllegalArgumentException(
                            "'%s' is invalid SocketMessage type!".formatted(m.getType()));
                }
            } catch (SocketTimeoutException ignored) {
            } catch (Exception e) {
                logger.log(Level.SEVERE, "", e);
                break;
            }
        }
    }

}
