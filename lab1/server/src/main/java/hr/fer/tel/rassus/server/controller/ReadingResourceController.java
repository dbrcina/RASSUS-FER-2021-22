package hr.fer.tel.rassus.server.controller;

import hr.fer.tel.rassus.server.dto.reading.RegisterReadingDto;
import hr.fer.tel.rassus.server.dto.reading.RetrieveReadingDto;
import hr.fer.tel.rassus.server.service.ReadingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping("/readings")
public class ReadingResourceController {

    private final ReadingService service;

    public ReadingResourceController(ReadingService readingService) {
        this.service = readingService;
    }

    @GetMapping("/{sensorId}")
    public ResponseEntity<?> retrieveReadings(@PathVariable("sensorId") long sensorId) {
        Collection<RetrieveReadingDto> readings = service.retrieveReadings(sensorId);
        if (readings == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/{sensorId}/{readingId}")
    public ResponseEntity<?> retrieveReading(@PathVariable("sensorId") long sensorId,
                                             @PathVariable("readingId") long readingId) {
        RetrieveReadingDto reading = service.retrieveReading(sensorId, readingId);
        if (reading == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reading);
    }

    @PostMapping("/{sensorId}")
    public ResponseEntity<?> registerReading(@PathVariable("sensorId") long sensorId,
                                             @RequestBody RegisterReadingDto registerReadingDto) {
        Long id = service.registerReading(sensorId, registerReadingDto);
        if (id == null) {
            return ResponseEntity.noContent().build();
        }
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

}