package hr.fer.tel.rassus.lab3;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements ApplicationRunner {

    private final ResourceLoader resourceLoader;
    private final ReadingRepository readingRepository;

    public DataLoader(ResourceLoader resourceLoader, ReadingRepository readingRepository) {
        this.resourceLoader = resourceLoader;
        this.readingRepository = readingRepository;
    }

    public void run(ApplicationArguments args) throws IOException {
        List<Reading> readings = new ArrayList<>(100);
        Resource readingsResource = resourceLoader.getResource("classpath:readings.csv");
        InputStream is = readingsResource.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("Temp")) continue;
            String[] parts = line.split(",", -1);
            Reading reading = new Reading();
            reading.setName("Temperature");
            reading.setUnit("C");
            reading.setValue(Double.parseDouble(parts[0]));
            readings.add(reading);
        }
        readingRepository.saveAll(readings);
    }

}
