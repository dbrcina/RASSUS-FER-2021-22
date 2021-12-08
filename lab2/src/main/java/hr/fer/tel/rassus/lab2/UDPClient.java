package hr.fer.tel.rassus.lab2;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

public final class UDPClient {

    private static final int MAX_UNCONFIRMED = 50;

    private static final Logger logger = Logger.getLogger(UDPClient.class.getName());

    private final InetAddress address;
    private final int port;
    private DatagramSocket socket;

    public UDPClient(String address, int port) throws UnknownHostException {
        this.address = InetAddress.getByName(address);
        this.port = port;
    }

    public void send() {
        if (socket == null || socket.isClosed()) {
            try {
                socket = new SimpleSimulatedDatagramSocket(0.3, 1000);
            } catch (SocketException e) {
                logger.severe("Client socket creation: " + e.getMessage());
                return;
            }
        }
        byte[] sendBuf = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
        try {
            socket.send(sendPacket);
        } catch (Exception e) {
            logger.severe("send() outside: " + e.getMessage());
            return;
        }
        // Receive ACK packet
        byte[] ackBytes = "ACK".getBytes(StandardCharsets.UTF_8);
        byte[] rcvBuf = new byte[ackBytes.length];
        int counter = 1;
        while (counter > 0) {
            logger.info("Unconfirmed: %d".formatted(counter));
            if (counter >= MAX_UNCONFIRMED) break;
            DatagramPacket ackPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
            try {
                socket.receive(ackPacket);
                if (Arrays.equals(ackPacket.getData(), ackBytes)) counter--;
            } catch (SocketTimeoutException e) {
                // Retransmission
                try {
                    socket.send(sendPacket);
                    counter++;
                } catch (Exception ex) {
                    logger.severe("send() inside: " + ex.getMessage());
                    break;
                }
            } catch (IOException e) {
                logger.severe("receive(): " + e.getMessage());
                break;
            }
        }
    }

    public void close() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

}
