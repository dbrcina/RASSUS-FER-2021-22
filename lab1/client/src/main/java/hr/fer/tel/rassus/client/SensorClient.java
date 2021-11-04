package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.client.retrofit.RetrofitImplementation;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SensorClient {

    private static final double LONGITUDE_LB = 15.87;
    private static final double LONGITUDE_UB = 16.0001;
    private static final double LATITUDE_LB = 45.75;
    private static final double LATITUDE_UB = 45.8501;
    private static final String IP = "127.0.0.1";
    private static final String URL = "http://localhost:8090";

    private final long id;
    private final double latitude;
    private final double longitude;
    private final String ip;
    private final int port;
    private final RestInterface rest;

    private long startTime;

    // Creates a new sensor and registers it.
    public SensorClient(double latitude, double longitude, String ip, int port, RestInterface rest) {
        this.id = rest.registerSensor(new RegisterSensorDto(latitude, longitude, ip, port));
        this.latitude = latitude;
        this.longitude = longitude;
        this.ip = ip;
        this.port = port;
        this.rest = rest;
    }

    public void start() {
        startTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
        while (true) ;
    }

    public static void main(String[] args) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Thread[] sensorThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            SensorClient sensor = new SensorClient(
                    random.nextDouble(LATITUDE_LB, LATITUDE_UB),
                    random.nextDouble(LONGITUDE_LB, LONGITUDE_UB),
                    IP,
                    8000 + i,
                    new RetrofitImplementation(URL));
            Thread sensorThread = new Thread(() -> {
                sensor.start();
                System.err.println("Gotovo");
            });
            sensorThread.setDaemon(true);
            sensorThreads[i] = sensorThread;
        }
        for (int i = 0; i < 10; i++) {
            sensorThreads[i].start();
        }
        while (true) ;
    }

}
