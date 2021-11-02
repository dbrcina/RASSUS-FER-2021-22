package hr.fer.tel.rassus.server.repository;

import hr.fer.tel.rassus.server.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
}
