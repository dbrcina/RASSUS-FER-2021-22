package hr.fer.tel.rassus.lab2.node.messages;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class AckMessage {

    private static AckMessage ackMessage;

    private final byte[] ackBuf;
    private final DatagramPacket ackPacket;

    private AckMessage() {
        ackBuf = "ACK".getBytes(StandardCharsets.UTF_8);
        ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
    }

    public DatagramPacket getAckPacket() {
        return ackPacket;
    }

    public boolean isEqual(byte[] ackBuf) {
        return Arrays.equals(this.ackBuf, ackBuf);
    }

    public static AckMessage getInstance() {
        if (ackMessage == null) {
            ackMessage = new AckMessage();
        }
        return ackMessage;
    }

}
