package hr.fer.tel.rassus.lab2;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

public class SensorClient {

    public static void main(String[] args) {
        Sensor sensor = createSensorFromInput();
        Properties consumerProps = initPropsForConsumer();
        Consumer<String, String> consumer = null;
        try {
            consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Arrays.asList("Register", "Command"));
            Duration timeout = Duration.ofMillis(100);
            while (true) {
//                for (ConsumerRecord<String, String> record : consumer.poll(timeout)) {
//                    if ()
//                }
            }
        } finally {
            if (consumer != null) {
                consumer.unsubscribe();
                consumer.close();
            }
        }
    }

    private static Sensor createSensorFromInput() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter id port: ");
            int[] input = Arrays.stream(sc.nextLine().strip().split("\\s+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            return new Sensor(input[0], "localhost", input[1]);
        }
    }

    private static Properties initPropsForConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "Sensors");
        return props;
    }

}
