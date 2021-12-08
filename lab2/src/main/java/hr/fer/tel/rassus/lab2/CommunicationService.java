package hr.fer.tel.rassus.lab2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CommunicationService implements Runnable {

    private final List<Double> readings;
    private final EmulatedSystemClock clock;
    private final AtomicBoolean running;
    private final Collection<UDPClient> connections;

    public CommunicationService(EmulatedSystemClock clock, AtomicBoolean running, Collection<UDPClient> connections) {
        this.clock = clock;
        this.running = running;
        this.connections = connections;
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
        while (running.get()) {
        }
    }

}
