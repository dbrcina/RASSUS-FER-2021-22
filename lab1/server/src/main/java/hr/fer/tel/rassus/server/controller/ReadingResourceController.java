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
        Collection<RetrieveReadingDto> retrieveReadingDtos = service.retrieveReadings(sensorId);
        if (retrieveReadingDtos == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(retrieveReadingDtos);
    }

    @GetMapping("/{sensorId}/{readingId}")
    public ResponseEntity<?> retrieveReading(@PathVariable("sensorId") long sensorId,
                                             @PathVariable("readingId") long readingId) {
        RetrieveReadingDto retrieveReadingDto = service.retrieveReading(sensorId, readingId);
        if (retrieveReadingDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(retrieveReadingDto);
    }

    @PostMapping("/{sensorId}")
    public ResponseEntity<?> registerReading(@PathVariable("sensorId") long sensorId,
                                             @RequestBody RegisterReadingDto registerReadingDto) {
        Long readingId = service.registerReading(sensorId, registerReadingDto);
        if (readingId == null) {
            return ResponseEntity.notFound().build();
        }
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(readingId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

}