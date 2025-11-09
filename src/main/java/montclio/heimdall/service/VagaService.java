package montclio.heimdall.service;

import jakarta.transaction.Transactional;
import montclio.heimdall.dto.VagaDTO.VagaFilter;
import montclio.heimdall.exception.DataConflictException;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.model.Vaga;
import montclio.heimdall.model.Zona;
import montclio.heimdall.repository.VagaRepository;
import montclio.heimdall.repository.ZonaRepository;
import montclio.heimdall.specification.VagaSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class VagaService {

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private ZonaRepository zonaRepository;


    private void validateAndCleanVagaData(Vaga vaga) {
        String codigoLimpo = (vaga.getCodigo() != null) ? vaga.getCodigo().trim().toUpperCase() : null;

        Vaga existingVaga = vagaRepository.findByCodigoIgnoreCase(codigoLimpo);

        if (vaga.getId() == null) {
            if (existingVaga != null) {
                throw new DataConflictException("Já existe uma Vaga cadastrada com o código '" + vaga.getCodigo() + "'.");
            }
        } else {
            if (existingVaga != null && !existingVaga.getId().equals(vaga.getId())) {
                throw new DataConflictException("Já existe uma Vaga cadastrada com o código '" + vaga.getCodigo() + "'.");
            }
        }
        vaga.setCodigo(codigoLimpo);

        if (vaga.getZona() == null || vaga.getZona().getId() == null) {
            throw new DataConflictException("O ID da Zona é obrigatório.");
        }

        Zona zona = zonaRepository.findById(vaga.getZona().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Zona com ID " + vaga.getZona().getId() + " não encontrada."));

        vaga.setZona(zona);

        // Status Ocupada:
        if (vaga.getOcupada() == null) {
            vaga.setOcupada(false);
        }
    }


    public Page<Vaga> findPageableVagas(VagaFilter filter, Pageable pageable) {
        Specification<Vaga> spec = VagaSpecification.withFilter(filter);
        return vagaRepository.findAll(spec, pageable);
    }

    public Vaga findById(Long id) {
        return vagaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga com ID " + id + " não encontrada."));
    }


    @Transactional
    public Vaga save(Vaga vaga) {
        validateAndCleanVagaData(vaga);
        return vagaRepository.save(vaga);
    }


    @Transactional
    public void delete(Long id) {
        Vaga vaga = findById(id);
        if (vaga.getOcupada()) {
            throw new DataConflictException("Não é possível excluir a vaga: ela está ocupada por uma motocicleta.");
        }
        vagaRepository.delete(vaga);
    }

    public Vaga findAndPrepareForEdit(Long id) {
        return this.findById(id);
    }
}