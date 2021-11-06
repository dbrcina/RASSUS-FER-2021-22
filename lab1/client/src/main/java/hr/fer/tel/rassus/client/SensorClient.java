package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.dto.reading.RegisterReadingDto;
import hr.fer.tel.rassus.client.dto.reading.RetrieveReadingDto;
import hr.fer.tel.rassus.client.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.client.dto.sensor.RetrieveSensorDto;
import hr.fer.tel.rassus.client.retrofit.RetrofitImplementation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class SensorClient {

    private static final Logger LOGGER = Logger.getLogger(SensorClient.class.getName());
    private static final double LONGITUDE_LB = 15.87;
    private static final double LONGITUDE_UB = 16.0001;
    private static final double LATITUDE_LB = 45.75;
    private static final double LATITUDE_UB = 45.8501;

    private final long id;
    private final int port;
    private final RestInterface rest;
    private final List<RetrieveReadingDto> readings = new ArrayList<>(100);

    private long startTime;

    // Creates a new sensor, registers it and prepares readings.
    public SensorClient(String ip, int port, RestInterface rest) throws IOException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double latitude = random.nextDouble(LATITUDE_LB, LATITUDE_UB);
        double longitude = random.nextDouble(LONGITUDE_LB, LONGITUDE_UB);
        this.port = port;
        this.rest = rest;
        id = rest.registerSensor(new RegisterSensorDto(latitude, longitude, ip, port));
        LOGGER.info(String.format("Sensor created successfully with id %d and port %d!", id, port));
        prepareReadings();
    }

    private void prepareReadings() throws IOException {
        try (InputStream is = SensorClient.class.getClassLoader().getResourceAsStream("readings.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Temp")) continue;
                String[] parts = line.split(",", -1);
                RetrieveReadingDto reading = new RetrieveReadingDto();
                reading.setTemperature(Double.parseDouble(parts[0]));
                reading.setPressure(Double.parseDouble(parts[1]));
                reading.setHumidity(Double.parseDouble(parts[2]));
                // If a gas reading is missing or is 0, set null value, otherwise parse it as is.
                reading.setCo((parts[3].equals("0") || parts[3].isEmpty()) ? null : Double.parseDouble(parts[3]));
                reading.setNo2((parts[4].equals("0") || parts[4].isEmpty()) ? null : Double.parseDouble(parts[4]));
                reading.setSo2((parts[5].equals("0") || parts[5].isEmpty()) ? null : Double.parseDouble(parts[5]));
                readings.add(reading);
            }
        }
    }

    public void start() throws IOException, InterruptedException {
        startTime = getCurrentTimeInSeconds();
        // gRPC server will be stopped when the program exists. See the implemention...
        new SimpleUnaryRPCServer(new ReadingService(readingSupplier), port).start();
        while (true) {
            RetrieveReadingDto reading = readingSupplier.get();
            LOGGER.info(String.format("New reading %s!", reading));
            RetrieveSensorDto closestSensor = rest.retrieveClosestSensor(id);
            RetrieveReadingDto closestSensorReading = null;
            if (closestSensor != null) {
                SimpleUnaryRPCClient client = new SimpleUnaryRPCClient(
                        port, closestSensor.getIp(), closestSensor.getPort());
                closestSensorReading = client.requestReading();
                client.stop();
            }
            RetrieveReadingDto calibratedReading = calibrateReadings(reading, closestSensorReading);
            LOGGER.info(String.format("Calibrated reading %s!", calibratedReading));
            rest.registerReading(id, new RegisterReadingDto(
                    calibratedReading.getTemperature(),
                    calibratedReading.getPressure(),
                    calibratedReading.getHumidity(),
                    calibratedReading.getCo(),
                    calibratedReading.getNo2(),
                    calibratedReading.getSo2()));
        }
    }

    private long getCurrentTimeInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    private final Supplier<RetrieveReadingDto> readingSupplier =
            () -> readings.get((int) (((getCurrentTimeInSeconds() - startTime) % 100) + 1));

    private RetrieveReadingDto calibrateReadings(RetrieveReadingDto reading, RetrieveReadingDto closestSensorReading) {
        if (closestSensorReading == null) {
            return reading;
        }
        RetrieveReadingDto calibratedReading = new RetrieveReadingDto();
        calibratedReading.setTemperature(0.5 * (reading.getTemperature() + closestSensorReading.getTemperature()));
        calibratedReading.setPressure(0.5 * (reading.getPressure() + closestSensorReading.getPressure()));
        calibratedReading.setHumidity(0.5 * (reading.getHumidity() + closestSensorReading.getHumidity()));
        calibrateGasReading(reading.getCo(), closestSensorReading.getCo(), calibratedReading::setCo);
        calibrateGasReading(reading.getNo2(), closestSensorReading.getNo2(), calibratedReading::setNo2);
        calibrateGasReading(reading.getSo2(), closestSensorReading.getSo2(), calibratedReading::setSo2);
        return calibratedReading;
    }

    private void calibrateGasReading(Double g1, Double g2, Consumer<Double> action) {
        if (g1 != null && g2 != null) {
            action.accept(0.5 * (g1 + g2));
        } else if (g1 != null) {
            action.accept(g1);
        } else {
            action.accept(g2);
        }
    }

    // Program expects port as a command line argument.
    public static void main(String[] args) throws IOException, InterruptedException {
        SensorClient client = new SensorClient(
                "127.0.0.1",
                Integer.parseInt(args[0]),
                new RetrofitImplementation("http://localhost:8090"));
        // Sleep for simulation purpose.
        Thread.sleep(5_000);
        client.start();
    }

}
