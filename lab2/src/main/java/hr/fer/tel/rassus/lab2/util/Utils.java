package hr.fer.tel.rassus.lab2.util;

import hr.fer.tel.rassus.lab2.node.message.SocketMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;

public class Utils {

    public static byte[] dataFromDatagramPacket(DatagramPacket packet) {
        return Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
    }

    public static DatagramPacket createSendPacket(SocketMessage m, InetAddress address, int port) throws IOException {
        byte[] sendBuf = SocketMessage.serialize(m);
        return new DatagramPacket(sendBuf, sendBuf.length, address, port);
    }

    public static void printStats(Collection<SocketMessage> messages) {

    }

}
