package hr.fer.tel.rassus.lab2.node.worker;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class SendWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(SendWorker.class.getName());

    private final DatagramSocket socket;
    private final AtomicBoolean running;
    private final BlockingQueue<DatagramPacket> sendQueue;

    public SendWorker(
            DatagramSocket socket,
            AtomicBoolean running,
            BlockingQueue<DatagramPacket> sendQueue) {
        this.socket = socket;
        this.running = running;
        this.sendQueue = sendQueue;
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                DatagramPacket sendPacket = sendQueue.poll(10, TimeUnit.MILLISECONDS);
                if (sendPacket != null) {
                    socket.send(sendPacket);
                }
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }
    }

}
