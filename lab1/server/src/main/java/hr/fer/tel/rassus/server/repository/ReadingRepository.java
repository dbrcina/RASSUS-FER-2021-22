package hr.fer.tel.rassus.server.repository;

import hr.fer.tel.rassus.server.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
}
