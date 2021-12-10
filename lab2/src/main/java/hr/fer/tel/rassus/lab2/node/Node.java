package hr.fer.tel.rassus.lab2.node;

import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;
import hr.fer.tel.rassus.lab2.network.OffsetBasedEmulatedSystemClock;
import hr.fer.tel.rassus.lab2.network.SimpleSimulatedDatagramSocket;
import hr.fer.tel.rassus.lab2.node.model.NodeModel;
import hr.fer.tel.rassus.lab2.node.worker.ReceiveWorker;
import hr.fer.tel.rassus.lab2.node.worker.SendWorker;
import hr.fer.tel.rassus.lab2.node.worker.UsefulWorker;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static hr.fer.tel.rassus.lab2.utils.KafkaConfig.consumerProps;
import static hr.fer.tel.rassus.lab2.utils.KafkaConfig.producerProps;

public final class Node {

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1_000);
    private static final double LOSS_RATE = 0.3;
    private static final int AVERAGE_DELAY = 1000;

    private final NodeModel model;
    private final AtomicLong timeOffset;
    private final EmulatedSystemClock clock;
    private final AtomicBoolean running;
    private final Collection<NodeModel> peerNetwork;
    private final BlockingQueue<DatagramPacket> sendQueue;

    public Node(int id, String host, int port) {
        model = new NodeModel(id, host, port);
        timeOffset = new AtomicLong();
        clock = new OffsetBasedEmulatedSystemClock(new EmulatedSystemClock(), timeOffset);
        running = new AtomicBoolean();
        peerNetwork = new HashSet<>();
        sendQueue = new LinkedBlockingQueue<>();
    }

    // This will work only when messages are ordered in the next order:
    // Start -> Register Register ... Register -> Stop
    public void loop() {
        try (Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps(Integer.toString(model.getId())));
             Producer<String, String> producer = new KafkaProducer<>(producerProps());
             DatagramSocket socket = new SimpleSimulatedDatagramSocket(model.getPort(), LOSS_RATE, AVERAGE_DELAY, running)) {
            consumer.subscribe(Arrays.asList("Command", "Register"));
            handleStart(consumer);
            producer.send(new ProducerRecord<>("Register", NodeModel.toJson(model)));
            registrationProcessing(consumer);
            new Thread(new UsefulWorker(clock, running, peerNetwork, sendQueue, model.getId())).start();
            new Thread(new ReceiveWorker(socket, running)).start();
            new Thread(new SendWorker(socket, running, sendQueue)).start();
            handleStop(consumer);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void handleStart(Consumer<String, String> consumer) {
        handleCommand(consumer, "start", o -> running.set(true));
    }

    private void handleStop(Consumer<String, String> consumer) {
        handleCommand(consumer, "stop", o -> {
            running.set(false);
            try {
                // So others can see that running is set to false.
                Thread.sleep(2_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleCommand(
            Consumer<String, String> consumer,
            String command,
            java.util.function.Consumer<Object> action) {
        boolean exit = false;
        do {
            for (ConsumerRecord<String, String> record : consumer.poll(POLL_TIMEOUT)) {
                if ("command".equalsIgnoreCase(record.topic())) {
                    if (command.equalsIgnoreCase(record.value())) {
                        action.accept(null);
                        exit = true;
                        break;
                    }
                }
            }
            consumer.commitAsync();
        } while (!exit);
    }

    private void registrationProcessing(Consumer<String, String> consumer) {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(POLL_TIMEOUT);
            if (records.isEmpty()) break;
            for (ConsumerRecord<String, String> record : records) {
                if ("register".equalsIgnoreCase(record.topic())) {
                    NodeModel otherModel = NodeModel.fromJson(record.value());
                    if (!otherModel.equals(model)) {
                        peerNetwork.add(otherModel);
                    }
                }
            }
            consumer.commitAsync();
        }
    }

}
