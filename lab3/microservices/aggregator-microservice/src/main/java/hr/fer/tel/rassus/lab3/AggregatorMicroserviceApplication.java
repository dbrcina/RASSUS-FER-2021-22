package hr.fer.tel.rassus.lab3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AggregatorMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregatorMicroserviceApplication.class, args);
    }

    @FeignClient("${microservices.humidity}")
    public interface HumidityMicroservice {
        @GetMapping("/readings/current")
        GetReadingDto getReading();
    }

    @FeignClient("${microservices.temperature}")
    public interface TemperatureMicroservice {
        @GetMapping("/readings/current")
        GetReadingDto getReading();
    }

}
