package hr.fer.tel.rassus.lab2.node;

import hr.fer.tel.rassus.lab2.network.ConcurrentEmulatedSystemClock;
import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;
import hr.fer.tel.rassus.lab2.network.SimpleSimulatedDatagramSocket;
import hr.fer.tel.rassus.lab2.node.message.DataMessage;
import hr.fer.tel.rassus.lab2.node.message.SocketMessage;
import hr.fer.tel.rassus.lab2.node.model.NodeModel;
import hr.fer.tel.rassus.lab2.node.worker.PrintWorker;
import hr.fer.tel.rassus.lab2.node.worker.ReceiveWorker;
import hr.fer.tel.rassus.lab2.node.worker.SendWorker;
import hr.fer.tel.rassus.lab2.util.Pair;
import hr.fer.tel.rassus.lab2.util.Utils;
import hr.fer.tel.rassus.lab2.util.Vector;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static hr.fer.tel.rassus.lab2.config.Configurations.AVERAGE_DELAY;
import static hr.fer.tel.rassus.lab2.config.Configurations.LOSS_RATE;

public class Node implements Runnable {

    private static final Logger logger = LogManager.getLogger(Node.class);

    private final NodeModel model;
    private final AtomicBoolean running;
    private final Collection<NodeModel> peerNetwork;
    private final Collection<DataMessage> tempMessages;
    private final Collection<DataMessage> scalarTimestampSorted;
    private final Collection<DataMessage> vectorTimestampSorted;

    private DatagramSocket socket;
    private ConcurrentEmulatedSystemClock clock;
    private Vector vectorTimestamp;
    private BlockingQueue<DatagramPacket> sendQueue;
    private Map<Integer, Pair<DatagramPacket, Long>> unAckPackets;
    private ScheduledExecutorService scheduler;
    private List<Double> readings;

    public Node(
            NodeModel model,
            AtomicBoolean running,
            Collection<NodeModel> peerNetwork,
            Collection<DataMessage> tempMessages,
            Collection<DataMessage> scalarTimestampSorted,
            Collection<DataMessage> vectorTimestampSorted) {
        this.model = model;
        this.running = running;
        this.peerNetwork = peerNetwork;
        this.tempMessages = tempMessages;
        this.scalarTimestampSorted = scalarTimestampSorted;
        this.vectorTimestampSorted = vectorTimestampSorted;
    }

    @Override
    public void run() {
        try {
            startup();
            loop();
        } catch (Exception e) {
            logger.log(Level.FATAL, "", e);
        } finally {
            scheduler.shutdown();
            socket.close();
        }

    }

    private void startup() throws IOException {
        logger.info("Startup...");
        socket = new SimpleSimulatedDatagramSocket(model.getPort(), LOSS_RATE, AVERAGE_DELAY, running);
        socket.setSoTimeout(10);
        clock = new ConcurrentEmulatedSystemClock(new EmulatedSystemClock());
        initVectorTimestamp();
        sendQueue = new LinkedBlockingQueue<>();
        unAckPackets = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(1);
        prepareReadings();
        startWorkers();
    }

    private void loop() throws IOException, InterruptedException {
        logger.info("Looping...");
        while (running.get()) {
            long start = System.currentTimeMillis();
            // Generate reading
            double reading = generateReading();
            // Save reading to temp collection
            tempMessages.add(new DataMessage(
                    model.getId(),
                    clock.currentTimeMillis(null),
                    vectorTimestamp.update(model.getId(), null, true),
                    reading));
            // Send reading to peer network
            for (NodeModel peer : peerNetwork) {
                SocketMessage m = new DataMessage(
                        model.getId(),
                        clock.currentTimeMillis(null),
                        vectorTimestamp.update(model.getId(), null, true),
                        reading);
                sendQueue.put(Utils.createSendPacket(m, InetAddress.getByName(peer.getAddress()), peer.getPort()));
            }
            // Do the work every 1s...
            Thread.sleep(Math.max(0, 1000 - (System.currentTimeMillis() - start)));
        }
    }

    private void initVectorTimestamp() {
        Collection<Integer> ids = new ArrayList<>();
        peerNetwork.forEach(m -> ids.add(m.getId()));
        ids.add(model.getId());
        vectorTimestamp = new Vector(ids.stream().mapToInt(i -> i).toArray());
    }

    private void prepareReadings() throws IOException {
        readings = new ArrayList<>(100);
        try (InputStream is = Node.class.getClassLoader().getResourceAsStream("readings.csv");
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
        }
    }

    private void startWorkers() {
        Runnable rcvWorker = new ReceiveWorker(
                model.getId(), clock, vectorTimestamp, socket, running, sendQueue, unAckPackets, tempMessages);
        Runnable sendWorker = new SendWorker(socket, running, sendQueue, unAckPackets);
        Runnable printWorker = new PrintWorker(tempMessages, scalarTimestampSorted, vectorTimestampSorted);
        new Thread(rcvWorker).start();
        new Thread(sendWorker).start();
        scheduler.scheduleAtFixedRate(printWorker, 5, 5, TimeUnit.SECONDS);
        logger.info("Successfully started all workers!");
    }

    private double generateReading() {
        long secondsPassed = TimeUnit.MILLISECONDS.toSeconds(clock.currentTimeMillis(null));
        return readings.get((int) (secondsPassed % readings.size()));
    }

}
