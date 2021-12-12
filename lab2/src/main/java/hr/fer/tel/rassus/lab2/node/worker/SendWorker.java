package hr.fer.tel.rassus.lab2.node.worker;

import hr.fer.tel.rassus.lab2.config.Configurations;
import hr.fer.tel.rassus.lab2.node.message.SocketMessage;
import hr.fer.tel.rassus.lab2.util.Pair;
import hr.fer.tel.rassus.lab2.util.Utils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendWorker implements Runnable {

    private static final Logger logger = LogManager.getLogger(SendWorker.class);

    private final DatagramSocket socket;
    private final AtomicBoolean running;
    private final BlockingQueue<DatagramPacket> sendQueue;
    private final Map<Integer, Pair<DatagramPacket, Long>> unAckPackets;

    public SendWorker(
            DatagramSocket socket,
            AtomicBoolean running,
            BlockingQueue<DatagramPacket> sendQueue,
            Map<Integer, Pair<DatagramPacket, Long>> unAckPackets) {
        this.socket = socket;
        this.running = running;
        this.sendQueue = sendQueue;
        this.unAckPackets = unAckPackets;
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                DatagramPacket sendPacket = sendQueue.poll(10, TimeUnit.MILLISECONDS);
                if (sendPacket != null) {
                    SocketMessage m = SocketMessage.deserialize(Utils.dataFromDatagramPacket(sendPacket));
                    switch (m.getType()) {
                        case DATA -> {
                            unAckPackets.put(m.getMessageId(), new Pair<>(sendPacket, System.currentTimeMillis()));
                            socket.send(sendPacket);
                        }
                        case ACK -> socket.send(sendPacket);
                        default -> throw new IllegalArgumentException(
                                "'%s' is invalid SocketMessage type!".formatted(m.getType()));
                    }
                }
                // Retransmission
                for (Map.Entry<Integer, Pair<DatagramPacket, Long>> entry : unAckPackets.entrySet()) {
                    Pair<DatagramPacket, Long> pair = entry.getValue();
                    long currentTime = System.currentTimeMillis();
                    long sentTime = pair.getV2();
                    // 4 * avg delay...see implementation of SimpleSimulatedDatagramSocket
                    if ((currentTime - sentTime) > 4 * Configurations.AVERAGE_DELAY) {
                        socket.send(pair.getV1());
                        pair.setV2(currentTime);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, "", e);
                break;
            }
        }
    }

}
