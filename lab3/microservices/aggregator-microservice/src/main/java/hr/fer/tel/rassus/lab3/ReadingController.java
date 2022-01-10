package hr.fer.tel.rassus.lab3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/readings")
public class ReadingController {

    @Value("${microservices.humidity}")
    private String humidityMicroservice;

    @Value("${microservices.temperature}")
    private String temperatureMicroservice;

    @Value("${temperature.unit}")
    private String temperatureUnit;

    private final RestTemplate restTemplate;

    public ReadingController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping("")
    public ResponseEntity<List<GetReadingDto>> fetchCurrentReadings() {
        List<GetReadingDto> readings = new ArrayList<>();
        GetReadingDto humidityReading = restTemplate.getForObject(humidityMicroservice, GetReadingDto.class);
        GetReadingDto temperatureReading = restTemplate.getForObject(temperatureMicroservice, GetReadingDto.class);
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
