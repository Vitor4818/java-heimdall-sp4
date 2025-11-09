package montclio.heimdall.service;

import jakarta.transaction.Transactional;
import montclio.heimdall.dto.TagRfidDTO.GetTagRfidDTO;
import montclio.heimdall.dto.TagRfidDTO.PostTagRfidDTO;
import montclio.heimdall.dto.TagRfidDTO.PutTagRfidDTO;
import montclio.heimdall.dto.TagRfidDTO.TagRfidFilter;
import montclio.heimdall.exception.DataConflictException;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.model.Motorcycle;
import montclio.heimdall.model.TagRfId;
import montclio.heimdall.repository.MotorcycleRepository;
import montclio.heimdall.repository.TagRfidRepository;
import montclio.heimdall.specification.TagRfidSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class TagRfidService {

    @Autowired
    private TagRfidRepository tagRfidRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MotorcycleRepository motorcycleRepository;

    //retorna todas as tags cadastradas
    public Page<GetTagRfidDTO> getAllTags(TagRfidFilter filter, Pageable page){
        Specification<TagRfId> spec = TagRfidSpecification.withFilter(filter);
        return tagRfidRepository.findAll(spec, page).map(GetTagRfidDTO::new);
    }

    //retorna tag por id
    @Cacheable(value = "tagById", key = "#id")
    public GetTagRfidDTO getTagById(Long id){
        return tagRfidRepository.findById(id).map(GetTagRfidDTO::new)
                .orElseThrow(()-> new ResourceNotFoundException("Tag com ID " + id + " não encontrada"));
    }

    //Cadastra nova tag
    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public TagRfId postTag(PostTagRfidDTO tagDTO) {
        Long motoId = tagDTO.motorcycle().getId();

        // Verifica se a moto existe
        var moto = motorcycleRepository.findById(motoId)
                .orElseThrow(() -> new ResourceNotFoundException("Moto com ID " + motoId + " não encontrada"));

        // Verifica se já existe tag para essa moto
        boolean exists = tagRfidRepository.existsByMotorcycleId(motoId);
        if (exists) {
            throw new DataConflictException("A tag para esta motocicleta já existe.");
        }

        // Cria nova tag com a moto válida
        TagRfId tagRfid = new TagRfId(tagDTO);
        tagRfid.setMotorcycle(moto);

        return tagRfidRepository.save(tagRfid);
    }


        //Atualiza Tag
        @Transactional
        @CacheEvict(value = {"tags", "tagById"}, allEntries = true)
        public void putTag(Long id, PutTagRfidDTO tagDTO){
            Long motoId = tagDTO.motorcycle().getId();

            var tag = tagRfidRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Tag com ID " + id + " não encontrada"));

            // Verifica se a moto existe
            var moto = motorcycleRepository.findById(motoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Moto com ID " + motoId + " não encontrada"));


            // Verifica se já existe tag para essa moto
            boolean exists = tagRfidRepository.existsByMotorcycleAndIdNot(moto, id);
            if (exists) {
                throw new DataConflictException("A tag para esta motocicleta já existe.");
            }

            Motorcycle oldMoto = tag.getMotorcycle();
            tag.updateData(tagDTO); // Aqui é onde a nova moto é ligada

            if (oldMoto != null) {
                cacheManager.getCache("motorcycleById").evict(oldMoto.getId());
            }

            Motorcycle newMoto = tag.getMotorcycle();
            if (newMoto != null) {
                cacheManager.getCache("motorcycleById").evict(newMoto.getId());
            }
            cacheManager.getCache("motorcycles").clear();
        }




    //Deleta uma tag
    @Transactional
    @CacheEvict(value = {"tags", "tagById"}, allEntries = true)
    public void deleteTag (Long id) {
        TagRfId tag =  tagRfidRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Tag com ID " + id + " não encontrada"));
        Motorcycle moto = tag.getMotorcycle();
        if (moto != null){
            moto.setTag(null);
        }
        tag.setMotorcycle(null);
        tagRfidRepository.deleteById(id);
    }

    // ----------------------------------------------------
    // NOVOS MÉTODOS PARA O MVC (CRUD DIRETO)
    // ----------------------------------------------------



    public Page<TagRfId> findAllPageable(TagRfidFilter filter, Pageable pageable) {
        Specification<TagRfId> spec = TagRfidSpecification.withFilter(filter);
        return tagRfidRepository.findAll(spec, pageable);
    }


    public TagRfId findById(Long id) {
        return tagRfidRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag RFID com ID " + id + " não encontrada."));
    }


    private void validateMotorcycleAssociation(Long motoId, Long tagId) {
        if (motoId == null) {
            return;
        }
        // Verifica se a Moto existe
        if (!motorcycleRepository.existsById(motoId)) {
            throw new ResourceNotFoundException("Moto com ID " + motoId + " não encontrada.");
        }
        // Verifica se já existe tag para essa moto
        boolean exists;
        if (tagId == null) {
            exists = tagRfidRepository.existsByMotorcycleId(motoId);
        } else {
            Motorcycle moto = motorcycleRepository.findById(motoId).get();
            exists = tagRfidRepository.existsByMotorcycleAndIdNot(moto, tagId);
        }
        if (exists) {
            throw new DataConflictException("A moto selecionada já possui uma tag RFID associada.");
        }
    }


    @Transactional
    @CacheEvict(value = {"tags", "tagById"}, allEntries = true)
    public TagRfId save(TagRfId tag) {
        Long motoId = (tag.getMotorcycle() != null) ? tag.getMotorcycle().getId() : null;
        validateMotorcycleAssociation(motoId, tag.getId());
        return tagRfidRepository.save(tag);
    }

}