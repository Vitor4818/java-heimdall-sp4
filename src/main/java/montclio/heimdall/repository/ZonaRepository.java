package montclio.heimdall.repository;

import montclio.heimdall.model.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
    Zona findByNomeIgnoreCase(String nomeLimpo);
}
