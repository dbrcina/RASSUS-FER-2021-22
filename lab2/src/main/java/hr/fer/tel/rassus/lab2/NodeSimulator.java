package hr.fer.tel.rassus.lab2;

import hr.fer.tel.rassus.lab2.node.Node;
import hr.fer.tel.rassus.lab2.node.message.DataMessage;
import hr.fer.tel.rassus.lab2.node.model.NodeModel;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static hr.fer.tel.rassus.lab2.config.Configurations.*;

public class NodeSimulator {

    private static final Logger logger = LogManager.getLogger(NodeSimulator.class);
    private static final AtomicBoolean running = new AtomicBoolean();
    private static final Collection<NodeModel> peerNetwork = new HashSet<>();
    private static final Collection<DataMessage> tempMessages = ConcurrentHashMap.newKeySet();
    private static final Collection<DataMessage> scalarTimestampSorted = new ConcurrentLinkedQueue<>();
    private static final Collection<DataMessage> vectorTimestampSorted = new ConcurrentLinkedQueue<>();

    private static NodeModel nm;

    public static void main(String[] args) {
        nm = createNodeModel();
        try (Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps(String.valueOf(nm.getId())));
             Producer<String, String> producer = new KafkaProducer<>(producerProps())) {
            consumer.subscribe(Arrays.asList("Command", "Register"));
            handleCommand(consumer, "Start", o -> running.set(true));
            registrationProcess(producer, consumer);
            new Thread(new Node(
                    nm,
                    running,
                    peerNetwork,
                    tempMessages,
                    scalarTimestampSorted,
                    vectorTimestampSorted)).start();
            handleCommand(consumer, "Stop", o -> {
                running.set(false);
                try {
                    logger.info("Sleeping for a bit so all the workers can exit properly...");
                    Thread.sleep(2_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info("Printing all received sorted data:");
                StringBuilder sb = new StringBuilder();
                sb.append("Sorted by scalar timestamps:\n");
                scalarTimestampSorted.forEach(m -> sb.append(m).append("\n"));
                sb.append("\n\n");
                sb.append("Sorted by vector timestamps:\n");
                vectorTimestampSorted.forEach(m -> sb.append(m).append("\n"));
                System.out.println(sb);
            });
        }
    }

    private static void handleCommand(
            Consumer<String, String> consumer,
            String command,
            java.util.function.Consumer<Void> action) {
        logger.info("Waiting for '{}' command...", command);
        boolean exit = false;
        do {
            for (ConsumerRecord<String, String> record : consumer.poll(CONSUMER_POLL_TIMEOUT)) {
                if ("command".equalsIgnoreCase(record.topic())) {
                    if (command.equalsIgnoreCase(record.value())) {
                        logger.info("Command '{}' received!", command);
                        action.accept(null);
                        exit = true;
                        break;
                    }
                }
            }
            consumer.commitAsync();
        } while (!exit);
    }

    private static void registrationProcess(Producer<String, String> producer, Consumer<String, String> consumer) {
        logger.info("Sending registration...");
        producer.send(new ProducerRecord<>("Register", NodeModel.toJson(nm)));
        logger.info("Registration sent successfully!");
        logger.info("Processing registrations from others...");
        try {
            // Wait for messages to arrive at topic Register
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(CONSUMER_POLL_TIMEOUT);
            if (records.isEmpty()) break;
            for (ConsumerRecord<String, String> record : records) {
                if ("register".equalsIgnoreCase(record.topic())) {
                    NodeModel otherModel = NodeModel.fromJson(record.value());
                    if (!otherModel.equals(nm)) {
                        peerNetwork.add(otherModel);
                    }
                }
            }
            consumer.commitAsync();
        }
        logger.info("Registrations processed successfully!");
    }

    private static NodeModel createNodeModel() {
        int id, port;
        String address = "localhost";
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter id port: ");
            int[] input = Arrays.stream(sc.nextLine().strip().split("\\s+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            id = input[0];
            port = input[1];
        }
        return new NodeModel(id, address, port);
    }

}
