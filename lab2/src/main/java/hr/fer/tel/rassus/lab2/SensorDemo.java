package hr.fer.tel.rassus.lab2;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SensorDemo {

    private static final Collection<String> topics;
    private static final Consumer<String, String> consumer;
    private static final Producer<String, String> producer;
    private static final AtomicBoolean running;
    private static final Duration timeout;

    static {
        topics = Arrays.asList("Register", "Command");
        consumer = new KafkaConsumer<>(consumerProps());
        producer = new KafkaProducer<>(producerProps());
        running = new AtomicBoolean();
        timeout = Duration.ofMillis(100);
    }

    private static Sensor sensor;

    public static void main(String[] args) {
        createSensorFromInput();
        try {
            consumer.subscribe(topics);
            boolean exit = false;
            do {
                for (ConsumerRecord<String, String> record : consumer.poll(timeout)) {
                    exit = handleTopic(record.topic(), record.value());
                    if (exit) break;
                }
                consumer.commitAsync();
            } while (!exit);
        } finally {
            consumer.unsubscribe();
            consumer.close();
            producer.close();
        }
    }

    private static boolean handleTopic(String topic, String value) {
        return switch (topic.toLowerCase()) {
            case "command" -> handleCommand(value);
            case "register" -> handleRegister(value);
            default -> throw new IllegalArgumentException("Wrong topic!");
        };
    }

    private static boolean handleCommand(String command) {
        command = command.toLowerCase();
        if (command.equals("start")) {
            if (!running.get()) {
                running.set(true);
                producer.send(new ProducerRecord<>("Register", Sensor.toJson(sensor)));
            }
        } else if (command.equals("stop")) {
            running.set(false);
            return true;
        }
        return false;
    }

    private static boolean handleRegister(String json) {
        return false;
    }

    private static void createSensorFromInput() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter id port: ");
            int[] input = Arrays.stream(sc.nextLine().strip().split("\\s+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            sensor = new Sensor(input[0], "localhost", input[1]);
        }
    }

    private static Properties consumerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "Sensors");
        return props;
    }

    private static Properties producerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

}
