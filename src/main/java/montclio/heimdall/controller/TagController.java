package montclio.heimdall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import montclio.heimdall.dto.TagRfidDTO.GetTagRfidDTO;
import montclio.heimdall.dto.TagRfidDTO.PostTagRfidDTO;
import montclio.heimdall.dto.TagRfidDTO.PutTagRfidDTO;
import montclio.heimdall.dto.TagRfidDTO.TagRfidFilter;
import montclio.heimdall.model.TagRfId;
import montclio.heimdall.service.TagRfidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tag RFID", description = "Operações relacionadas às tags RFID")
public class TagController {

    @Autowired
    private TagRfidService tagService;

    @GetMapping
    @Operation(
            summary = "Lista todas as tags",
            description = "Retorna uma lista paginada de todas as tags cadastradas, com filtros opcionais."
    )
    public ResponseEntity<Page<GetTagRfidDTO>> getAllTags(TagRfidFilter filter,
                                                          @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable page) {
        return ResponseEntity.ok(tagService.getAllTags(filter, page));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtém uma tag por ID",
            description = "Retorna os dados de uma tag específica pelo seu ID."
    )
    public ResponseEntity<GetTagRfidDTO> getTagById(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PostMapping
    @Operation(
            summary = "Cadastra uma nova tag",
            description = "Recebe os dados de uma nova tag e a salva no sistema."
    )
    public ResponseEntity<Void> createTag(@Valid @RequestBody PostTagRfidDTO tagDTO) {
        TagRfId savedTagRfid = tagService.postTag(tagDTO);
        return ResponseEntity.created(URI.create("/tags/" + savedTagRfid.getId())).build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza uma tag existente",
            description = "Atualiza os dados de uma tag com base no ID fornecido."
    )
    public ResponseEntity<Void> updateTag(@PathVariable Long id,
                                          @Valid @RequestBody PutTagRfidDTO tagRfidDTO) {
        tagService.putTag(id, tagRfidDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remove uma tag",
            description = "Deleta uma tag do sistema com base no seu ID."
    )
    public ResponseEntity<Void> deleteTagById(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
