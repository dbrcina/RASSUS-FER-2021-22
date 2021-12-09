package hr.fer.tel.rassus.lab2.node.communication.messages;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public final class AckMessage {

    private static AckMessage ackMessage;

    private final byte[] ackBuf;
    private final DatagramPacket ackPacket;

    private AckMessage() {
        ackBuf = "ACK".getBytes(StandardCharsets.UTF_8);
        ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
    }

    public byte[] getAckBuf() {
        return ackBuf;
    }

    public DatagramPacket getAckPacket() {
        return ackPacket;
    }

    public static AckMessage getInstance() {
        if (ackMessage == null) {
            ackMessage = new AckMessage();
        }
        return ackMessage;
    }

}
