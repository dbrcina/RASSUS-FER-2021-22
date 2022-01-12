package hr.fer.tel.rassus.lab3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/readings")
public class ReadingController {

    private final HumidityMicroservice humidityMicroservice;
    private final TemperatureMicroservice temperatureMicroservice;
    private final String temperatureUnit;

    public ReadingController(HumidityMicroservice humidityMicroservice,
                             TemperatureMicroservice temperatureMicroservice,
                             @Value("${temperature.unit}") String temperatureUnit) {
        this.humidityMicroservice = humidityMicroservice;
        this.temperatureMicroservice = temperatureMicroservice;
        this.temperatureUnit = temperatureUnit;
    }

    @GetMapping("")
    public ResponseEntity<List<GetReadingDto>> fetchCurrentReadings() {
        List<GetReadingDto> readings = new ArrayList<>();
        GetReadingDto humidityReading = humidityMicroservice.getReading();
        GetReadingDto temperatureReading = temperatureMicroservice.getReading();
        if (humidityReading != null) {
            readings.add(humidityReading);
        }
        if (temperatureReading != null) {
            transformTemperatureUnit(temperatureReading);
            readings.add(temperatureReading);
        }
        return ResponseEntity.ok(readings);
    }

    private void transformTemperatureUnit(GetReadingDto getReadingDto) {
        String dtoTempUnit = getReadingDto.getUnit();
        if (temperatureUnit.equalsIgnoreCase(dtoTempUnit)) return;
        double value = switch (dtoTempUnit) {
            case "C" -> getReadingDto.getValue() + 273.15;
            case "K" -> getReadingDto.getValue() - 273.15;
            default -> throw new RuntimeException("Invalid temperature type: '{%s}'".formatted(dtoTempUnit));
        };
        getReadingDto.setUnit(temperatureUnit);
        getReadingDto.setValue(value);
    }

}
