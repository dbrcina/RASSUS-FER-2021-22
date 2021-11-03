package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.dto.reading.RegisterReadingDto;
import hr.fer.tel.rassus.client.dto.reading.RetrieveReadingDto;
import hr.fer.tel.rassus.client.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.client.retrofit.RetrofitImplementation;

import java.util.Collection;

public class Main {

    public static void main(String[] args) {
        RestInterface rest = new RetrofitImplementation("http://localhost:8090");
        long sensorId1 = rest.registerSensor(new RegisterSensorDto(45.75, 15.87, "127.0.0.1", 8080));
        long sensorId2 = rest.registerSensor(new RegisterSensorDto(45.76, 15.88, "127.0.0.1", 8081));
        long sensorId3 = rest.registerSensor(new RegisterSensorDto(45.76, 16.88, "127.0.0.1", 8082));
        long readingId4SensorId1 = rest.registerReading(
                sensorId1, new RegisterReadingDto(32, 1109, 40, 547.0, null, 18.0));
        long readingId5SensorId1 = rest.registerReading(
                sensorId1, new RegisterReadingDto(33, 1109, 40, 547.0, 1000.0, null));
        long readingId6SensorId2 = rest.registerReading(
                sensorId2, new RegisterReadingDto(27, 1109, 40, null, 900.0, 18.0));
        long invalidRegister = rest.registerReading(
                5, new RegisterReadingDto(32, 1109, 40, 547.0, null, 18.0));
        Collection<RetrieveReadingDto> readingsForSensorId1 = rest.retrieveReadings(sensorId1);
        Collection<RetrieveReadingDto> readingsForSensorId2 = rest.retrieveReadings(sensorId2);
        Collection<RetrieveReadingDto> readingsForSensorId3 = rest.retrieveReadings(sensorId3);
        rest.retrieveReadings(5);
        System.out.println(readingsForSensorId1);
        System.out.println(readingsForSensorId2);
        System.out.println(readingsForSensorId3);
    }
}
