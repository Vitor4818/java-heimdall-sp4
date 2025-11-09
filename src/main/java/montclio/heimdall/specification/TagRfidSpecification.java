package montclio.heimdall.specification;

import jakarta.persistence.criteria.Predicate;
import montclio.heimdall.dto.TagRfidDTO.TagRfidFilter;
import montclio.heimdall.model.TagRfId;
import montclio.heimdall.constants.TagRfIdFields; // Assumindo que TagRfIdFields existe
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TagRfidSpecification {
    public static Specification<TagRfId> withFilter(TagRfidFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro por frequência
            String freq = filter.frequencia();
            if (freq != null && !freq.trim().isEmpty()) {
                String valor = freq.trim().toLowerCase();
                predicates.add(
                        cb.like(cb.lower(root.get(TagRfIdFields.FREQUENCIA)), "%" + valor + "%")
                );
            }

            // 2. Filtro por banda
            String banda = filter.banda();
            if (banda != null && !banda.trim().isEmpty()) {
                String valor = banda.trim().toLowerCase();
                predicates.add(
                        cb.like(cb.lower(root.get(TagRfIdFields.BANDA)), "%" + valor + "%")
                );
            }

            // 3. Filtro por aplicação
            String aplicacao = filter.aplicacao();
            if (aplicacao != null && !aplicacao.trim().isEmpty()) {
                String valor = aplicacao.trim().toLowerCase();
                predicates.add(
                        cb.like(cb.lower(root.get(TagRfIdFields.APLICACAO)), "%" + valor + "%")
                );
            }

            // 4. Filtro por ID da moto relacionada
            Optional.ofNullable(filter.motorcycleId())
                    .ifPresent(motoId -> predicates.add(
                            cb.equal(root.get(TagRfIdFields.MOTORCYCLE).get("id"), motoId)
                    ));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}