package hr.fer.tel.rassus.lab2.node.worker;

import hr.fer.tel.rassus.lab2.node.message.AckMessage;
import hr.fer.tel.rassus.lab2.node.message.DataMessage;
import hr.fer.tel.rassus.lab2.node.message.SocketMessage;
import hr.fer.tel.rassus.lab2.util.Utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class SendWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(SendWorker.class.getName());

    private final DatagramSocket socket;
    private final AtomicBoolean running;
    private final BlockingQueue<DatagramPacket> sendQueue;
    private final Queue<AckMessage> ackRcvQueue;
    private final Map<Integer, DatagramPacket> unAckPacketsMap;

    public SendWorker(
            DatagramSocket socket,
            AtomicBoolean running,
            BlockingQueue<DatagramPacket> sendQueue,
            Queue<AckMessage> ackRcvQueue) {
        this.socket = socket;
        this.running = running;
        this.sendQueue = sendQueue;
        this.ackRcvQueue = ackRcvQueue;
        unAckPacketsMap = new HashMap<>();
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                DatagramPacket sendPacket = sendQueue.poll(10, TimeUnit.MILLISECONDS);
                if (sendPacket != null) {
                    SocketMessage m = SocketMessage.deserialize(Utils.dataFromDatagramPacket(sendPacket));
                    switch (m.getType()) {
                        case DATA -> unAckPacketsMap.put()
                    }
                    socket.send(sendPacket);
                }
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }
    }

}
