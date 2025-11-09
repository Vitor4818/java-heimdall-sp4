package montclio.heimdall.repository;

import montclio.heimdall.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, Long>,
        JpaSpecificationExecutor<Vaga> {

    Vaga findByCodigoIgnoreCase(String codigo);
}