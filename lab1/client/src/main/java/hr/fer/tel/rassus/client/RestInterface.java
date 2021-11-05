package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.dto.reading.RegisterReadingDto;
import hr.fer.tel.rassus.client.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.client.dto.sensor.RetrieveSensorDto;

public interface RestInterface {

    long registerSensor(RegisterSensorDto registerSensorDto);

    RetrieveSensorDto retrieveClosestSensor(long id);

    long registerReading(long sensorId, RegisterReadingDto registerReadingDto);

}
