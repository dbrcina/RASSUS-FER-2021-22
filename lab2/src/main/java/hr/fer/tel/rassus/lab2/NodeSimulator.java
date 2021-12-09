package hr.fer.tel.rassus.lab2;

import hr.fer.tel.rassus.lab2.node.Node;

import java.util.Arrays;
import java.util.Scanner;

public final class NodeSimulator {

    public static void main(String[] args) {
        Scanner sc = null;
        int id, port;
        String address = "localhost";
        try {
            sc = new Scanner(System.in);
            System.out.print("Enter id port: ");
            int[] input = Arrays.stream(sc.nextLine().strip().split("\\s+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            id = input[0];
            port = input[1];
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
        new Node(id, address, port).loop();
    }

}
