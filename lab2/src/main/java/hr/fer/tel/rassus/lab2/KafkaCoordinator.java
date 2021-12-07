package hr.fer.tel.rassus.lab2;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Scanner;

public final class KafkaCoordinator {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (Producer<String, String> producer = new KafkaProducer<>(props);
             Scanner sc = new Scanner(System.in)) {

            System.out.print("Press ANY KEY to Start: ");
            sc.nextLine();
            System.out.println("Starting the coordinator!");
            producer.send(new ProducerRecord<>("Command", "Start"));

            System.out.print("Press ANY KEY to Stop: ");
            sc.nextLine();
            System.out.println("Stopping the coordinator!");
            producer.send(new ProducerRecord<>("Command", "Stop"));

            System.out.println("Exiting!");
        }
    }

}
