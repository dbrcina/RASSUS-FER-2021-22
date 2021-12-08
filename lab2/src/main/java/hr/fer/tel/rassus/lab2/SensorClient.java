package hr.fer.tel.rassus.lab2;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SensorClient {

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1_000);

    private final Sensor sensor;
    private final Consumer<String, String> consumer;
    private final Producer<String, String> producer;
    private final EmulatedSystemClock clock;
    private final AtomicBoolean running;
    private final Collection<Sensor> peerNetwork;
    private final Collection<UDPClient> connections;
    private final CommunicationService communicationService;
    private final UDPServer server;

    public SensorClient(int id, String address, int port) {
        sensor = new Sensor(id, address, port);
        consumer = new KafkaConsumer<>(KafkaConfig.consumerProps("Sensor" + id));
        producer = new KafkaProducer<>(KafkaConfig.producerProps());
        clock = new EmulatedSystemClock();
        running = new AtomicBoolean();
        peerNetwork = new HashSet<>();
        connections = new ArrayList<>();
        communicationService = new CommunicationService(clock, running, connections);
        server = new UDPServer(port, running);
    }

    // This will work only when messages are ordered in the next order:
    // Start -> Register Register ... Register -> Stop
    public void loop() throws Exception {
        try {
            consumer.subscribe(Arrays.asList("Command", "Register"));
            // Start
            waitForStart();
            // Register
            producer.send(new ProducerRecord<>("Register", Sensor.toJson(sensor)));
            // Process registrations
            registrationProcessing();
            // Stop
            waitForStop();
        } finally {
            consumer.unsubscribe();
            consumer.close();
            producer.close();
        }
    }

    private void waitForStart() {
        do {
            for (ConsumerRecord<String, String> record : consumer.poll(POLL_TIMEOUT)) {
                if (record.topic().equalsIgnoreCase("command")) {
                    if ("start".equalsIgnoreCase(record.value())) {
                        running.set(true);
                        break;
                    }
                }
            }
            consumer.commitAsync();
        } while (!running.get());
    }

    private void registrationProcessing() throws UnknownHostException {
        // Wait for messages to arrive at the topic Register, but don't commit.
        consumer.poll(POLL_TIMEOUT);
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(POLL_TIMEOUT);
            if (records.isEmpty()) break;
            for (ConsumerRecord<String, String> record : records) {
                if ("register".equalsIgnoreCase(record.topic())) {
                    Sensor otherSensor = Sensor.fromJson(record.value());
                    if (!otherSensor.equals(sensor)) {
                        peerNetwork.add(otherSensor);
                        connections.add(new UDPClient(otherSensor.getAddress(), otherSensor.getPort()));
                    }
                }
            }
            consumer.commitAsync();
        }
    }

    private void waitForStop() {
        do {
            for (ConsumerRecord<String, String> record : consumer.poll(POLL_TIMEOUT)) {
                if ("command".equalsIgnoreCase(record.topic())) {
                    if ("stop".equalsIgnoreCase(record.value())) {
                        running.set(false);
                        break;
                    }
                }
            }
            consumer.commitAsync();
        } while (running.get());
    }

    public static void main(String[] args) throws Exception {
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
        new SensorClient(id, address, port).loop();
    }

}
