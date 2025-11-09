package montclio.heimdall.repository;

import montclio.heimdall.model.Motorcycle;
import montclio.heimdall.model.TagRfId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRfidRepository extends JpaRepository <TagRfId, Long>, JpaSpecificationExecutor<TagRfId> {
    boolean existsByMotorcycleId(Long id);

    boolean existsByMotorcycleAndIdNot(Motorcycle motorcycle, Long tagId);

    Optional<TagRfId> findByMotorcycle(Motorcycle motorcycle);
}
