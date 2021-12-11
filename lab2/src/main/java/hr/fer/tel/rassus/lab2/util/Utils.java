package hr.fer.tel.rassus.lab2.util;

import java.net.DatagramPacket;
import java.util.Arrays;

public class Utils {

    public static byte[] dataFromDatagramPacket(DatagramPacket packet) {
        return Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength());
    }

}
