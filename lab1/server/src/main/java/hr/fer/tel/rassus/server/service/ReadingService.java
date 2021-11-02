package hr.fer.tel.rassus.server.service;

import hr.fer.tel.rassus.server.dto.reading.RegisterReadingDto;
import hr.fer.tel.rassus.server.dto.reading.RetrieveReadingDto;
import hr.fer.tel.rassus.server.model.Reading;
import hr.fer.tel.rassus.server.model.Sensor;
import hr.fer.tel.rassus.server.repository.ReadingRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class ReadingService {

    private final ReadingRepository repository;
    private final SensorService sensorService;

    public ReadingService(ReadingRepository repository, SensorService sensorService) {
        this.repository = repository;
        this.sensorService = sensorService;
    }

    public Collection<RetrieveReadingDto> retrieveReadings(long sensorId) {
        Sensor sensor = sensorService.retrieveSensorInternal(sensorId);
        if (sensor == null) return null;
        return sensor.getReadings().stream()
                .map(reading -> toRetrieveReadingDto(reading, true))
                .collect(Collectors.toList());
    }

    public RetrieveReadingDto retrieveReading(long sensorId, long readingId) {
        Sensor sensor = sensorService.retrieveSensorInternal(sensorId);
        if (sensor == null) return null;
        return sensor.getReadings().stream()
                .filter(reading -> reading.getId() == readingId)
                .findFirst()
                .map(reading -> toRetrieveReadingDto(reading, false))
                .orElse(null);
    }

    public Long registerReading(long sensorId, RegisterReadingDto registerReadingDto) {
        Sensor sensor = sensorService.retrieveSensorInternal(sensorId);
        if (sensor == null) return null;
        Reading reading = new Reading();
        reading.setTemperature(registerReadingDto.getTemperature());
        reading.setPressure(registerReadingDto.getPressure());
        reading.setHumidity(registerReadingDto.getHumidity());
        reading.setCo(registerReadingDto.getCo());
        reading.setNo2(registerReadingDto.getNo2());
        reading.setSo2(registerReadingDto.getSo2());
        reading.setSensor(sensor);
        reading = repository.save(reading);
        return reading.getId();
    }

    private RetrieveReadingDto toRetrieveReadingDto(Reading reading, boolean includeId) {
        return new RetrieveReadingDto(
                includeId ? reading.getId() : null,
                reading.getTemperature(),
                reading.getPressure(),
                reading.getHumidity(),
                reading.getCo(),
                reading.getNo2(),
                reading.getSo2());
    }

}
