package montclio.heimdall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import montclio.heimdall.dto.MotorcycleDTO.GetMotorcycleDTO;
import montclio.heimdall.dto.MotorcycleDTO.MotorcycleFilter;
import montclio.heimdall.dto.MotorcycleDTO.PostMotorcycleDTO;
import montclio.heimdall.dto.MotorcycleDTO.PutMotorcycleDTO;
import montclio.heimdall.model.Motorcycle;
import montclio.heimdall.service.MotorcycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/motorcycles")
@Tag(name = "Motorcycles", description = "Gerencia motos cadastradas no sistema")
public class MotorcycleController {

    @Autowired
    private MotorcycleService motorcycleService;


    @Operation(
            summary = "Lista todas as motos",
            description = "Retorna uma lista paginada de todas as motos cadastradas. Pode ser filtrada por parâmetros e ordenada."
    )
    @GetMapping
    public ResponseEntity<Page<GetMotorcycleDTO>> getAllMotorcycles(
            MotorcycleFilter filter,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable page
    ) {
        return ResponseEntity.ok(motorcycleService.getAllMotorcycles(filter, page));
    }

    @Operation(
            summary = "Busca moto por ID",
            description = "Retorna os dados de uma moto com base no ID fornecido."
    )
    @GetMapping("/{id}")
    public ResponseEntity<GetMotorcycleDTO> getMotorcycleById(@PathVariable Long id) {
        return ResponseEntity.ok(motorcycleService.getMotorcycleById(id));
    }

    @Operation(
            summary = "Cadastra uma nova moto",
            description = "Cria uma nova moto no sistema com os dados fornecidos. Retorna a URI da moto criada."
    )
    @PostMapping
    public ResponseEntity<Void> createMotorcycle(@Valid @RequestBody PostMotorcycleDTO motorcycleDTO) {
        Motorcycle savedMotorcycle = motorcycleService.postMotorcycle(motorcycleDTO);
        return ResponseEntity.created(URI.create("/motorcycles/" + savedMotorcycle.getId())).build();
    }

    @Operation(
            summary = "Atualiza uma moto existente",
            description = "Atualiza os dados de uma moto com base no ID fornecido."
    )
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMotorcycle(
            @PathVariable Long id,
            @Valid @RequestBody PutMotorcycleDTO motorcycleDTO
    ) {
        motorcycleService.putMotorcycle(id, motorcycleDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remove uma moto",
            description = "Deleta uma moto com base no ID fornecido."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMotorcycleById(@PathVariable Long id) {
        motorcycleService.deleteMotorcycle(id);
        return ResponseEntity.noContent().build();
    }
}
