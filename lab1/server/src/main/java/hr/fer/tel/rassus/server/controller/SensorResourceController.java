package hr.fer.tel.rassus.server.controller;

import hr.fer.tel.rassus.server.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.server.dto.sensor.RetrieveSensorDto;
import hr.fer.tel.rassus.server.service.SensorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/sensors")
public class SensorResourceController {

    private final SensorService sensorService;

    public SensorResourceController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping("")
    public Collection<RetrieveSensorDto> retrieveSensors() {
        return sensorService.retrieveSensors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> retrieveSensor(@PathVariable("id") long id) {
        RetrieveSensorDto retrieveSensorDto = sensorService.retrieveSensor(id);
        if (retrieveSensorDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(retrieveSensorDto);
    }

    @GetMapping("/closest/{id}")
    public ResponseEntity<?> retrieveClosestSensor(@PathVariable("id") long id) {
        RetrieveSensorDto fromSensor = sensorService.retrieveSensor(id);
        if (fromSensor == null) {
            return ResponseEntity.notFound().build();
        }
        RetrieveSensorDto closestSensor = sensorService.retrieveClosestSensor(fromSensor);
        if (closestSensor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(closestSensor);
    }

    @PostMapping("")
    public ResponseEntity<?> registerSensor(@RequestBody RegisterSensorDto registerSensorDto) {
        long id = sensorService.registerSensor(registerSensorDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

}
