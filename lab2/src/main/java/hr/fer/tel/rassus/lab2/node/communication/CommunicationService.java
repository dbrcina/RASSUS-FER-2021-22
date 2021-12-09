package hr.fer.tel.rassus.lab2.node.communication;

import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;
import hr.fer.tel.rassus.lab2.node.communication.messages.DataMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class CommunicationService implements Runnable {

    private final int senderId;
    private final EmulatedSystemClock clock;
    private final AtomicBoolean running;
    private final AtomicLong scalarValue;
    private final List<UDPClient> clients;
    private final List<Double> readings;


    public CommunicationService(
            int senderId,
            EmulatedSystemClock clock,
            AtomicBoolean running,
            AtomicLong scalarValue,
            List<UDPClient> clients) {
        this.senderId = senderId;
        this.clock = clock;
        this.running = running;
        this.scalarValue = scalarValue;
        this.clients = clients;
        readings = new ArrayList<>(100);
        try (InputStream is = CommunicationService.class.getClassLoader().getResourceAsStream("readings.csv");
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
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public double getReading() {
        long secondsPassed = TimeUnit.MILLISECONDS.toSeconds(clock.currentTimeMillis());
        return readings.get((int) (secondsPassed % readings.size()) + 1);
    }

    @Override
    public void run() {
        Thread[] workers = new Thread[clients.size()];
        int seq = -1;
        while (running.get()) {
            seq++;
            double reading = getReading();
            for (int i = 0; i < workers.length; i++) {
                UDPClient client = clients.get(i);
                int finalSeq = seq;
                Runnable work = () -> {
                    long scalarValue = this.scalarValue.get() + clock.currentTimeMillis();
                    DataMessage dataMessage = new DataMessage(senderId, finalSeq, reading, scalarValue);
                    client.send(dataMessage);
                };
                Thread worker = new Thread(work);
                worker.start();
                workers[i] = worker;
            }
            for (Thread worker : workers) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
