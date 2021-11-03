package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.dto.reading.RegisterReadingDto;
import hr.fer.tel.rassus.client.dto.reading.RetrieveReadingDto;
import hr.fer.tel.rassus.client.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.client.dto.sensor.RetrieveSensorDto;

import java.util.Collection;

public interface RestInterface {

    long registerSensor(RegisterSensorDto registerSensorDto);

    RetrieveSensorDto retrieveClosestSensor(long id);

    Collection<RetrieveSensorDto> retrieveSensors();

    long registerReading(long sensorId, RegisterReadingDto registerReadingDto);

    Collection<RetrieveReadingDto> retrieveReadings(long sensorId);

}
