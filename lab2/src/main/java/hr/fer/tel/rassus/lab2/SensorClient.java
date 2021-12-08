package hr.fer.tel.rassus.lab2;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class SensorClient {

    private final Sensor sensor;
    private final Consumer<String, String> consumer;
    private final Producer<String, String> producer;
    private final EmulatedSystemClock clock;
    private final List<Double> readings;
    private final Supplier<Double> readingSupplier;
    private final Set<Sensor> peerNetwork;
    private final AtomicBoolean running;
    private final UDPServer server;

    private boolean startMsgReceived;

    public SensorClient(int id, String address, int port) throws IOException {
        sensor = new Sensor(id, address, port);
        consumer = new KafkaConsumer<>(consumerProps());
        producer = new KafkaProducer<>(producerProps());
        clock = new EmulatedSystemClock();
        readings = new ArrayList<>(100);
        prepareReadings();
        readingSupplier = () -> {
            long secondsPassed = TimeUnit.MILLISECONDS.toSeconds(clock.currentTimeMillis());
            return readings.get((int) (secondsPassed % readings.size()) + 1);
        };
        peerNetwork = new HashSet<>();
        running = new AtomicBoolean();
        server = new UDPServer(port, running);
    }

    private void prepareReadings() throws IOException {
        try (InputStream is = SensorClient.class.getClassLoader().getResourceAsStream("readings.csv");
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

    private Properties consumerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "Sensor%d".formatted(sensor.getId()));
        return props;
    }

    private Properties producerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    public void loop() {
        try {
            consumer.subscribe(Arrays.asList("Register", "Command"));
            Duration pollTimeout = Duration.ofMillis(100);
            boolean exit = false;
            do {
                for (ConsumerRecord<String, String> record : consumer.poll(pollTimeout)) {
                    handleRecord(record);
                    exit = startMsgReceived && !running.get();
                    if (exit) break;
                }
                consumer.commitAsync();
            } while (!exit);
        } finally {
            consumer.unsubscribe();
            consumer.close();
            producer.close();
            server.close();
        }
    }

    private void handleRecord(ConsumerRecord<String, String> record) {
        String topic = record.topic().toLowerCase();
        String value = record.value();
        switch (topic) {
            case "command" -> handleTopicCommand(value);
            case "register" -> handleTopicRegister(value);
            default -> throw new IllegalArgumentException("Wrong topic!");
        }
    }

    private void handleTopicCommand(String command) {
        command = command.toLowerCase();
        if (command.equals("start")) {
            if (!running.get()) {
                startMsgReceived = true;
                running.set(true);
                new Thread(server::loop).start();
                producer.send(new ProducerRecord<>("Register", Sensor.toJson(sensor)));
            }
        } else if (command.equals("stop")) {
            running.set(false);
            // Close UDP connections.
            // Print.
        }
    }

    private void handleTopicRegister(String json) {
        if (!startMsgReceived) return;
        Sensor otherSensor = Sensor.fromJson(json);
        if (!otherSensor.equals(sensor)) {
            peerNetwork.add(otherSensor);
        }
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
