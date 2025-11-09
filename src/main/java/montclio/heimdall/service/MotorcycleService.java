package montclio.heimdall.service;

import jakarta.transaction.Transactional;
import montclio.heimdall.dto.MotorcycleDTO.GetMotorcycleDTO;
import montclio.heimdall.dto.MotorcycleDTO.MotorcycleFilter;
import montclio.heimdall.dto.MotorcycleDTO.PostMotorcycleDTO;
import montclio.heimdall.dto.MotorcycleDTO.PutMotorcycleDTO;
import montclio.heimdall.exception.DataConflictException;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.model.Motorcycle;
import montclio.heimdall.model.TagRfId;
import montclio.heimdall.model.Vaga;
import montclio.heimdall.repository.TagRfidRepository;
import montclio.heimdall.repository.VagaRepository;
import montclio.heimdall.specification.MotorcycleSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import montclio.heimdall.repository.MotorcycleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MotorcycleService {
    @Autowired
    private MotorcycleRepository motorcycleRepository;
    @Autowired private VagaRepository vagaRepository;
    @Autowired private TagRfidRepository tagRfidRepository;

    //Retorna todas as motos cadastradas
    public Page<GetMotorcycleDTO> getAllMotorcycles(MotorcycleFilter filter, Pageable page) {
        Specification<Motorcycle> spec = MotorcycleSpecification.withFilter(filter);
        return motorcycleRepository.findAll(spec, page).map(GetMotorcycleDTO::new);
    }

    //Retorna moto por id
    @Cacheable (value = "motorcycleById", key = "#id")
    public GetMotorcycleDTO getMotorcycleById(Long id){
        return motorcycleRepository.findById(id).map(GetMotorcycleDTO::new).
                orElseThrow(()-> new ResourceNotFoundException("Moto com ID " + id + " não encontrada"));
    }

    //Cadastra uma nova moto
    @Transactional
    @CacheEvict(value = "motorcycles", allEntries = true)
    public Motorcycle postMotorcycle(PostMotorcycleDTO motorcycleDTO){
        Motorcycle motorcycle = new Motorcycle(motorcycleDTO);
        return motorcycleRepository.save(motorcycle);
    }

    //Atualiza dados Moto
    @Transactional
    @CacheEvict(value = {"motorcycles", "motorcycleById"}, allEntries = true)
    public void putMotorcycle ( Long id, PutMotorcycleDTO motorcycleDTO){
        var moto = motorcycleRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Moto com ID " + id + " não encontrada"));
        moto.updateData(motorcycleDTO);
    }

    //deleta os dados da moto
    @Transactional
    @CacheEvict(value = {"motorcycles", "motorcycleById"}, allEntries = true)
    public void deleteMotorcycle(Long id) {
        Motorcycle moto = motorcycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Moto com id: " + id + " não encontrada"));

        TagRfId tag = moto.getTag();
        if (tag != null) {
            if (tag.getMotorcycle() != null) {
                tag.setMotorcycle(null);
            }
        }

        moto.setTag(null);
        motorcycleRepository.deleteById(id);
    }

    // ----------------------------------------------------
    // NOVOS MÉTODOS PARA O MVC (CRUD DIRETO)
    // ----------------------------------------------------



    /** Centraliza a validação de unicidade para Placa e Chassi. */
    private void validateUniqueness(Motorcycle moto) {

        // 1. Limpeza de Placa e Chassi (DRY)
        String placaLimpa = moto.getPlate() != null ? moto.getPlate().trim().toUpperCase() : null;
        String chassiLimpo = moto.getChassiNumber() != null ? moto.getChassiNumber().trim().toUpperCase() : null;

        moto.setPlate(placaLimpa);
        moto.setChassiNumber(chassiLimpo);

        // --- VALIDAÇÃO DE PLACA (CREATE vs UPDATE) ---
        if (motorcycleRepository.existsByPlateAndIdNot(placaLimpa, moto.getId() != null ? moto.getId() : -1L)) {
            throw new DataConflictException("Essa placa já está cadastrada para outra moto.");
        }

        // --- VALIDAÇÃO DE CHASSI (CREATE vs UPDATE) ---
        if (motorcycleRepository.existsByChassiNumberAndIdNot(chassiLimpo, moto.getId() != null ? moto.getId() : -1L)) {
            throw new DataConflictException("Esse número de chassi já está cadastrado para outra moto.");
        }
    }

    // ----------------------------------------------------
    // MÉTODOS DE SUPORTE MVC (ADICIONADOS)
    // ----------------------------------------------------

    public Page<Motorcycle> findAllPageable(MotorcycleFilter filter, Pageable pageable) {
        Specification<Motorcycle> spec = MotorcycleSpecification.withFilter(filter);
        return motorcycleRepository.findAll(spec, pageable);
    }

    public Motorcycle findById(Long id) {
        return motorcycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Moto com ID " + id + " não encontrada."));
    }

    public Motorcycle findAndPrepareForEdit(Long id) {
        return this.findById(id);
    }

    @Transactional
    @CacheEvict(value = {"motorcycles", "motorcycleById"}, allEntries = true)
    public Motorcycle save(Motorcycle moto) {

        validateUniqueness(moto);
        Long oldVagaId = null;

        if (moto.getId() != null) {
            Motorcycle motoExistente = findById(moto.getId());
            oldVagaId = motoExistente.getVaga() != null ? motoExistente.getVaga().getId() : null;
        }

        Long newVagaId = moto.getVaga() != null ? moto.getVaga().getId() : null;

        if (oldVagaId != null && !oldVagaId.equals(newVagaId)) {
            Vaga vagaAntiga = vagaRepository.findById(oldVagaId).orElse(null);
            if (vagaAntiga != null) {
                vagaAntiga.setOcupada(false);
                vagaRepository.save(vagaAntiga);
            }
        }

        if (newVagaId != null && !newVagaId.equals(oldVagaId)) {
            Vaga vagaNova = vagaRepository.findById(newVagaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vaga com ID " + newVagaId + " não existe."));
            if (vagaNova.getOcupada()) {
                throw new DataConflictException("Vaga já está ocupada por outra moto.");
            }
            vagaNova.setOcupada(true);
            vagaRepository.save(vagaNova);
        }
        return motorcycleRepository.save(moto);
    }

    @Transactional
    public void delete(Long id) {
        Motorcycle motoParaRemover = findById(id);

        // Libera a Vaga e QUEBRA O VÍNCULO BIDIRECIONAL
        if (motoParaRemover.getVaga() != null) {
            Vaga vagaOcupada = vagaRepository.findById(motoParaRemover.getVaga().getId()).orElse(null);
            if (vagaOcupada != null) {
                vagaOcupada.setOcupada(false);
                vagaOcupada.setMoto(null);
                vagaRepository.save(vagaOcupada);
            }
        }

        // Desvincula a Tag
        TagRfId tagVinculada = tagRfidRepository.findByMotorcycle(motoParaRemover).orElse(null);
        if (tagVinculada != null) {
            tagVinculada.setMotorcycle(null);
            tagRfidRepository.save(tagVinculada);
        }

        // Deleta a Moto
        motorcycleRepository.delete(motoParaRemover);
    }
}
