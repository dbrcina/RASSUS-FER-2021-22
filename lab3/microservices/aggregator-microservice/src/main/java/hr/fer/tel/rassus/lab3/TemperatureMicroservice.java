package hr.fer.tel.rassus.lab3;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;

@Lazy
@FeignClient("${microservices.temperature}")
public interface TemperatureMicroservice {
    @GetMapping("/readings/current")
    GetReadingDto getReading();
}
