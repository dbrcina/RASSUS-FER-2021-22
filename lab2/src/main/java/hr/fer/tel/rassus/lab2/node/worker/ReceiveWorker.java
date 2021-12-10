package hr.fer.tel.rassus.lab2.node.worker;

import hr.fer.tel.rassus.lab2.node.messages.AckMessage;
import hr.fer.tel.rassus.lab2.node.messages.DataMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class ReceiveWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(ReceiveWorker.class.getName());
    private static final int RCV_BUF_SIZE = 256;

    private final DatagramSocket socket;
    private final AtomicBoolean running;
    private final byte[] rcvBuf;

    public ReceiveWorker(DatagramSocket socket, AtomicBoolean running) {
        this.socket = socket;
        this.running = running;
        rcvBuf = new byte[RCV_BUF_SIZE];
    }

    @Override
    public void run() {
        DatagramPacket rcvPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
        while (running.get()) {
            try {
                socket.receive(rcvPacket);
                byte[] dataBuf = Arrays.copyOfRange(rcvPacket.getData(), rcvPacket.getOffset(), rcvPacket.getLength());
                if (AckMessage.getInstance().isEqual(dataBuf)) {

                } else {
                    DataMessage dataMessage = DataMessage.deserialize(dataBuf);
                }
            } catch (SocketTimeoutException ignored) {
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }
    }

}
