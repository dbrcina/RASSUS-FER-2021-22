package hr.fer.tel.rassus.lab3;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/readings")
public class ReadingController {

    private final ReadingRepository readingRepository;

    public ReadingController(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<GetReadingDto> fetchCurrentReading() {
        Reading reading = readingRepository.findById(System.currentTimeMillis() % 100).get();
        return ResponseEntity.ok(GetReadingDto.fromReading(reading));
    }

}
