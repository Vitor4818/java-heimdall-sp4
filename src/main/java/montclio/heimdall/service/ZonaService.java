package montclio.heimdall.service;

import jakarta.transaction.Transactional;
import montclio.heimdall.exception.DataConflictException;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.model.Zona;
import montclio.heimdall.repository.ZonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZonaService {

    @Autowired
    private ZonaRepository zonaRepository;

    // Lista todas as zonas (Como são apenas 3, não há necessidade de estar em pageable e nem filtro de pesquisa)
    public List<Zona> findAll() {
        return zonaRepository.findAll();
    }

    // READ (Por ID)
    public Zona findById(Long id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona com ID " + id + " não encontrada."));
    }

    @Transactional
    public Zona save(Zona zona) {
        if (zona.getId() != null) {
            validateUniqueName(zona.getNome(), zona.getId());
        }
        return zonaRepository.save(zona);
    }

    private void validateUniqueName(String nome, Long id) {
        String nomeLimpo = (nome != null) ? nome.trim().toLowerCase() : null;

        Zona existingZona = zonaRepository.findByNomeIgnoreCase(nomeLimpo);

        if (existingZona != null && !existingZona.getId().equals(id)) {
            throw new DataConflictException("Já existe outra Zona cadastrada com o nome '" + nome + "'.");
        }
    }
}
