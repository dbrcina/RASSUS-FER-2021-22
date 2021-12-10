package hr.fer.tel.rassus.lab2.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleSimulatedDatagramSocket extends DatagramSocket {

    private final double lossRate;
    private final int averageDelay;
    private final Random random;
    private final AtomicBoolean running;

    //use this constructor for the server side (no timeout)
    public SimpleSimulatedDatagramSocket(int port, double lossRate, int averageDelay, AtomicBoolean running) throws SocketException, IllegalArgumentException {
        super(port);
        this.running = running;
        random = new Random();

        this.lossRate = lossRate;
        this.averageDelay = averageDelay;

        //set time to wait for answer
        super.setSoTimeout(0);
    }

    //use this constructor for the client side (timeout = 4 * averageDelay)
    public SimpleSimulatedDatagramSocket(double lossRate, int averageDelay, AtomicBoolean running) throws SocketException, IllegalArgumentException {
        this.running = running;
        random = new Random();

        this.lossRate = lossRate;
        this.averageDelay = averageDelay;

        //set time to wait for answer
        super.setSoTimeout(4 * averageDelay);
    }

    @Override
    public void send(DatagramPacket packet) throws IOException {
        if (random.nextDouble() >= lossRate) {
            //delay is uniformely distributed between 0 and 2*averageDelay
            new Thread(new OutgoingDatagramPacket(packet, (long) (2 * averageDelay * random.nextDouble()), running)).start();
        }
    }

    /**
     * Inner class for internal use.
     */
    private class OutgoingDatagramPacket implements Runnable {

        private final DatagramPacket packet;
        private final long time;
        private final AtomicBoolean running;

        private OutgoingDatagramPacket(DatagramPacket packet, long time, AtomicBoolean running) {
            this.packet = packet;
            this.time = time;
            this.running = running;
        }

        @Override
        public void run() {
            try {
                //simulate network delay
                Thread.sleep(time);
                if (running.get()) {
                    SimpleSimulatedDatagramSocket.super.send(packet);
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (IOException ex) {
                Logger.getLogger(SimulatedDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

