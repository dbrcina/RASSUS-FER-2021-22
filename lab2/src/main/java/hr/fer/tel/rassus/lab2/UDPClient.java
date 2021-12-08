package hr.fer.tel.rassus.lab2;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public final class UDPClient {

    private static final Logger logger = Logger.getLogger(UDPClient.class.getName());

    private final InetAddress address;
    private final int port;
    private DatagramSocket socket;

    public UDPClient(String address, int port) throws UnknownHostException {
        this.address = InetAddress.getByName(address);
        this.port = port;
    }

    public void send() {
        if (socket == null) {
            try {
                logger.info("Opening connection towards server at port %d...".formatted(port));
                socket = new SimpleSimulatedDatagramSocket(0.3, 1000);
                logger.info("Connection opened!");
            } catch (SocketException e) {
                logger.severe("Client socket creation: " + e.getMessage());
                System.exit(-1);
            }
        }
        byte[] sendBuf = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
        try {
            socket.send(sendPacket);
        } catch (Exception e) {
            logger.severe("send() outside: " + e.getMessage());
        }
        // Receive ACK packet
        int counter = 1;
        byte[] rcvBuf = new byte[256];
        while (counter > 0) {
            DatagramPacket ackPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
            try {
                socket.receive(ackPacket);
                System.out.printf("Received %s%n", new String(ackPacket.getData(), ackPacket.getOffset(),
                        ackPacket.getLength()));
                counter--;
            } catch (SocketTimeoutException e) {
                // Retransmission
                try {
                    socket.send(sendPacket);
                } catch (Exception ex) {
                    logger.severe("send() inside: " + ex.getMessage());
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
            logger.info("Connection closed!");
        }
    }

}
