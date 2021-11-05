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

    private final SensorService service;

    public SensorResourceController(SensorService sensorService) {
        this.service = sensorService;
    }

    @GetMapping("")
    public Collection<RetrieveSensorDto> retrieveSensors() {
        return service.retrieveSensors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> retrieveSensor(@PathVariable("id") long id) {
        RetrieveSensorDto sensor = service.retrieveSensor(id);
        if (sensor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sensor);
    }

    @GetMapping("/closest/{id}")
    public ResponseEntity<?> retrieveClosestSensor(@PathVariable("id") long id) {
        RetrieveSensorDto closestSensor = service.retrieveClosestSensor(id);
        if (closestSensor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(closestSensor);
    }

    @PostMapping("")
    public ResponseEntity<?> registerSensor(@RequestBody RegisterSensorDto registerSensorDto) {
        long id = service.registerSensor(registerSensorDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

}
