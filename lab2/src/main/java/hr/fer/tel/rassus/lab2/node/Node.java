package hr.fer.tel.rassus.lab2.node;

import hr.fer.tel.rassus.lab2.node.model.NodeModel;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static hr.fer.tel.rassus.lab2.utils.KafkaConfig.consumerProps;
import static hr.fer.tel.rassus.lab2.utils.KafkaConfig.producerProps;

public final class Node {

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1_000);

    private final NodeModel model;
    private final AtomicBoolean running;

    public Node(int id, String host, int port) {
        model = new NodeModel(id, host, port);
        running = new AtomicBoolean();
    }

    // This will work only when messages are ordered in the next order:
    // Start -> Register Register ... Register -> Stop
    public void loop() {
        try (Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps(Integer.toString(model.getId())));
             Producer<String, String> producer = new KafkaProducer<>(producerProps())) {
            consumer.subscribe(Arrays.asList("Command", "Register"));
            handleStart(consumer);
            producer.send(new ProducerRecord<>("Register", NodeModel.toJson(model)));
            registrationProcessing(consumer);
            handleStop(consumer);
        }
    }

    private void handleStart(Consumer<String, String> consumer) {
        handleCommand(consumer, "start", o -> running.set(true));
    }

    private void handleStop(Consumer<String, String> consumer) {
        handleCommand(consumer, "stop", o -> running.set(false));
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
        // Wait for messages to arrive at the topic Register, but don't commit.
        consumer.poll(POLL_TIMEOUT);
        // This will only work after the data folder is deleted. Fix in future?
        consumer.seekToBeginning(consumer.assignment());
        do {
            for (ConsumerRecord<String, String> record : consumer.poll(POLL_TIMEOUT)) {
                if ("register".equalsIgnoreCase(record.topic())) {
                    NodeModel otherModel = NodeModel.fromJson(record.value());
                    if (!otherModel.equals(model)) {
                        // handle connections
                    }
                }
            }
            consumer.commitAsync();
        } while (true);
    }

}
