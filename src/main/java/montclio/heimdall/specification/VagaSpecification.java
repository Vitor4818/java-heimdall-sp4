package montclio.heimdall.specification;

import jakarta.persistence.criteria.Predicate;
import montclio.heimdall.dto.VagaDTO.VagaFilter;
import montclio.heimdall.model.Vaga;
import montclio.heimdall.constants.VagaFields;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VagaSpecification {
    public static Specification<Vaga> withFilter (VagaFilter filter){
        return ((root, query, cb)  -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por CÓDIGO DA VAGA
            Optional.ofNullable(filter.codigo())
                    .map(String::toLowerCase)
                    .ifPresent(codigo -> predicates.add(
                            cb.like(cb.lower(root.get(VagaFields.CODIGO)), "%" + codigo + "%")
                    ));

            // Filtro por ID DA ZONA
            Optional.ofNullable(filter.zonaId()).ifPresent(zonaId -> {
                // Acessa o relacionamento 'zona' e busca o 'id'
                predicates.add(cb.equal(root.get(VagaFields.ZONA).get("id"), zonaId));
            });

            // Filtro por STATUS DE OCUPAÇÃO
            Optional.ofNullable(filter.isOccupied()).ifPresent(isOccupied -> {
                predicates.add(cb.equal(root.get(VagaFields.IS_OCCUPIED), isOccupied));
            });

            var arrayPredicates = predicates.toArray(new Predicate[0]);
            return cb.and(arrayPredicates);
        });
    }
}