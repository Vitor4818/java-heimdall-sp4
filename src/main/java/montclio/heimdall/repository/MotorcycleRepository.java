package montclio.heimdall.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import montclio.heimdall.model.Motorcycle;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MotorcycleRepository extends JpaRepository<Motorcycle, Long>, JpaSpecificationExecutor<Motorcycle> {
    boolean existsByPlateAndIdNot(String plate, Long id);

    boolean existsByChassiNumberAndIdNot(String chassiNumber, Long id);
}
