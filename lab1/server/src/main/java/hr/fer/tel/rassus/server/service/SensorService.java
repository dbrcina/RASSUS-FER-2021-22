package hr.fer.tel.rassus.server.service;

import hr.fer.tel.rassus.server.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.server.dto.sensor.RetrieveSensorDto;
import hr.fer.tel.rassus.server.model.Sensor;
import hr.fer.tel.rassus.server.repository.SensorRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.Math.*;

@Service
public class SensorService {

    private final SensorRepository repository;

    public SensorService(SensorRepository repository) {
        this.repository = repository;
    }

    public Collection<RetrieveSensorDto> retrieveSensors() {
        return repository.findAll().stream()
                .map(sensor -> toRetrieveSensorDto(sensor, true))
                .collect(Collectors.toList());
    }

    public RetrieveSensorDto retrieveSensor(long id) {
        return toRetrieveSensorDto(retrieveSensorInternal(id), false);
    }

    // Package-private
    Sensor retrieveSensorInternal(long id) {
        return repository.findById(id).orElse(null);
    }

    public RetrieveSensorDto retrieveClosestSensor(long id) {
        Sensor fromSensor = retrieveSensorInternal(id);
        if (fromSensor == null) return null;
        Collection<Sensor> sensors = repository.findAll();
        Sensor closestSensor = null;
        double minDistance = Double.MAX_VALUE;
        int R = 6371;
        double lon1 = fromSensor.getLongitude();
        double lat1 = fromSensor.getLatitude();
        for (Sensor sensor : sensors) {
            if (sensor.getId() != fromSensor.getId()) {
                double lon2 = sensor.getLongitude();
                double lat2 = sensor.getLatitude();
                double dlon = lon2 - lon1;
                double dlat = lat2 - lat1;
                double a = pow(sin(dlat / 2), 2) + cos(lat1) * cos(lat2) * pow(sin(dlon / 2), 2);
                double c = 2 * atan2(sqrt(a), sqrt(1 - a));
                double d = R * c;
                if (d < minDistance) {
                    minDistance = d;
                    closestSensor = sensor;
                }
            }
        }
        return toRetrieveSensorDto(closestSensor, false);
    }

    public long registerSensor(RegisterSensorDto registerSensorDto) {
        Sensor sensor = new Sensor();
        sensor.setLatitude(registerSensorDto.getLatitude());
        sensor.setLongitude(registerSensorDto.getLongitude());
        sensor.setIp(registerSensorDto.getIp());
        sensor.setPort(registerSensorDto.getPort());
        sensor = repository.save(sensor);
        return sensor.getId();
    }

    private RetrieveSensorDto toRetrieveSensorDto(Sensor sensor, boolean includeId) {
        if (sensor == null) return null;
        return new RetrieveSensorDto(
                includeId ? sensor.getId() : null,
                sensor.getLatitude(),
                sensor.getLongitude(),
                sensor.getIp(),
                sensor.getPort());
    }

}
