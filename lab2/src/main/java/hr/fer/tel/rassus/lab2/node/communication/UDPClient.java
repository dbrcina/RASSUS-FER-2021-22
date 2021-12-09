package hr.fer.tel.rassus.lab2.node.communication;

import hr.fer.tel.rassus.lab2.network.SimpleSimulatedDatagramSocket;
import hr.fer.tel.rassus.lab2.node.communication.messages.AckMessage;
import hr.fer.tel.rassus.lab2.node.communication.messages.DataMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class UDPClient {

    private static final Logger logger = Logger.getLogger(UDPClient.class.getName());

    private final InetAddress address;
    private final int port;
    private final AtomicBoolean running;
    private DatagramSocket socket;

    public UDPClient(String address, int port, AtomicBoolean running) throws UnknownHostException {
        this.address = InetAddress.getByName(address);
        this.port = port;
        this.running = running;
    }

    public void send(DataMessage dataMessage) {
        createSocket();
        byte[] sendBuf;
        try {
            sendBuf = serializeDataMessage(dataMessage);
        } catch (IOException e) {
            logger.severe("serializeDataMessage(): " + e.getMessage());
            return;
        }
        DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, address, port);
        try {
            socket.send(sendPacket);
        } catch (Exception e) {
            logger.severe("send() outside: " + e.getMessage());
            return;
        }
        int counter = 1;
        byte[] rcvBuf = new byte[16];
        while (running.get() && counter > 0) {
            DatagramPacket ackPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
            try {
                socket.receive(ackPacket);
                if (Arrays.equals(AckMessage.getAckBuf(), Arrays.copyOf(ackPacket.getData(), ackPacket.getLength()))) {
                    counter--;
                }
            } catch (SocketTimeoutException e) {
                try {
                    socket.send(sendPacket);
                    counter++;
                } catch (Exception ex) {
                    logger.severe("send() inside: " + ex.getMessage());
                    return;
                }
            } catch (IOException e) {
                logger.severe("receive(): " + e.getMessage());
                return;
            }
        }
    }

    private byte[] serializeDataMessage(DataMessage dataMessage) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream objos = new ObjectOutputStream(bos)) {
            objos.writeObject(dataMessage);
            return bos.toByteArray();
        }
    }

    private void createSocket() {
        if (socket == null) {
            try {
                socket = new SimpleSimulatedDatagramSocket(0.3, 1000);
            } catch (SocketException e) {
                logger.severe("Client socket creation: " + e.getMessage());
                System.exit(-1);
            }
        }
    }

    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

}
