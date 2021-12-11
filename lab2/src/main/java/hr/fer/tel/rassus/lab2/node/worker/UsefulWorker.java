package hr.fer.tel.rassus.lab2.node.worker;

import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;
import hr.fer.tel.rassus.lab2.node.message.DataMessage;
import hr.fer.tel.rassus.lab2.node.message.SocketMessage;
import hr.fer.tel.rassus.lab2.node.model.NodeModel;
import hr.fer.tel.rassus.lab2.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsefulWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(UsefulWorker.class.getName());

    private final int nodeId;
    private final EmulatedSystemClock clock;
    private final AtomicBoolean running;
    private final Collection<NodeModel> peerNetwork;
    private final BlockingQueue<DatagramPacket> sendQueue;
    private final List<Double> readings;

    public UsefulWorker(
            int nodeId,
            EmulatedSystemClock clock,
            AtomicBoolean running,
            Collection<NodeModel> peerNetwork,
            BlockingQueue<DatagramPacket> sendQueue) {
        this.nodeId = nodeId;
        this.clock = clock;
        this.running = running;
        this.peerNetwork = peerNetwork;
        this.sendQueue = sendQueue;
        readings = new ArrayList<>(100);
        try (InputStream is = UsefulWorker.class.getClassLoader().getResourceAsStream("readings.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Temp")) continue;
                String[] parts = line.split(",", -1);
                if (parts[3].isEmpty()) {
                    parts[3] = "0";
                }
                readings.add(Double.parseDouble(parts[3]));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "", e);
            System.exit(-1);
        }
    }

    public double generateReading() {
        long secondsPassed = TimeUnit.MILLISECONDS.toSeconds(clock.currentTimeMillis());
        return readings.get((int) (secondsPassed % readings.size()));
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                double reading = generateReading();
                for (NodeModel peer : peerNetwork) {
                    SocketMessage m = new DataMessage(nodeId, reading);
                    sendQueue.put(Utils.createSendPacket(m, InetAddress.getByName(peer.getAddress()), peer.getPort()));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "", e);
                break;
            }
        }
    }

}
