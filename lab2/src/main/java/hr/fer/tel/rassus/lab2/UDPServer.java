package hr.fer.tel.rassus.lab2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class UDPServer {

    private static final Logger logger = Logger.getLogger(UDPServer.class.getName());

    private final int port;
    private final AtomicBoolean running;
    private DatagramSocket socket;

    public UDPServer(int port, AtomicBoolean running) {
        this.port = port;
        this.running = running;
    }

    public void loop() {
        if (socket == null) {
            try {
                socket = new SimpleSimulatedDatagramSocket(port, 0.3, 1000);
            } catch (SocketException e) {
                logger.severe("Server socket creation: " + e.getMessage());
                System.exit(-1);
            }
        }
        byte[] rcvBuf = new byte[256];
        byte[] sendBuf = "ACK".getBytes(StandardCharsets.UTF_8);
        while (running.get()) {
            DatagramPacket rcvPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
            try {
                socket.receive(rcvPacket);
            } catch (IOException e) {
                logger.severe("receive(): " + e.getMessage());
                break;
            }
            DatagramPacket sendPacket = new DatagramPacket(
                    sendBuf, sendBuf.length, rcvPacket.getAddress(), rcvPacket.getPort());
            try {
                socket.send(sendPacket);
            } catch (Exception e) {
                logger.severe("send(): " + e.getMessage());
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
