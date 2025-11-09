package montclio.heimdall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import montclio.heimdall.dto.UserDTO.GetUserDTO;
import montclio.heimdall.dto.UserDTO.PostUserDTO;
import montclio.heimdall.dto.UserDTO.PutUserDTO;
import montclio.heimdall.dto.UserDTO.UserFilter;
import montclio.heimdall.model.User;
import montclio.heimdall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Operações relacionadas aos usuários do sistema")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(
            summary = "Listar todos os usuários",
            description = "Lista paginada de usuários cadastrados, com suporte a filtros."
    )
    public ResponseEntity<Page<GetUserDTO>> getAllUsers(
            UserFilter filter,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable page) {
        return ResponseEntity.ok(userService.getAllUsers(filter, page));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os dados de um usuário específico a partir do seu ID."
    )
    public ResponseEntity<GetUserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar um novo usuário",
            description = "Cria um novo usuário no sistema com os dados fornecidos."
    )
    public ResponseEntity<Void> createUser(@RequestBody @Valid PostUserDTO dto) {
        User savedUser = userService.postUser(dto);
        return ResponseEntity.created(URI.create("/users/" + savedUser.getId())).build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar um usuário",
            description = "Atualiza os dados de um usuário existente com base no ID."
    )
    public ResponseEntity<Void> updateUser(@RequestBody @Valid PutUserDTO dto, @PathVariable Long id) {
        userService.putUser(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar um usuário",
            description = "Remove um usuário do sistema com base no ID fornecido."
    )
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
