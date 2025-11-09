package montclio.heimdall.service;

import jakarta.transaction.Transactional;
import montclio.heimdall.dto.UserDTO.GetUserDTO;
import montclio.heimdall.dto.UserDTO.PostUserDTO;
import montclio.heimdall.dto.UserDTO.PutUserDTO;
import montclio.heimdall.dto.UserDTO.UserFilter;
import montclio.heimdall.exception.DataConflictException;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.model.User;
import montclio.heimdall.model.UserCategory;
import montclio.heimdall.repository.UserCategoryRepository;
import montclio.heimdall.repository.UserRepository;
import montclio.heimdall.specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCategoryRepository categoryRepository;


    // ----------------------------------------------------
    // MÉTODOS EXISTENTES (REST/API)
    // ----------------------------------------------------

    public Page<GetUserDTO> getAllUsers(UserFilter filter, Pageable page) {
        Specification<User> spec = UserSpecification.withFilter(filter);
        return userRepository.findAll(spec, page).map(GetUserDTO::new);
    }

    @Cacheable(value = "userById", key = "#id")
    public GetUserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(GetUserDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com ID " + id + " não encontrado"));
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User postUser(PostUserDTO dto) {
        // Validações complexas de email e CPF antes de criar (Ótimo!)
        boolean exist = userRepository.existsByEmail(dto.email());
        if (exist){
            throw new DataConflictException("Esse e-mail já está sendo usado por outro usuário");
        }
        if (userRepository.existsByCpf(dto.cpf())) {
            throw new DataConflictException("Esse CPF ja esta cadastrado");
        }

        UserCategory category = categoryRepository.findById(dto.userCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria com id: " + dto.userCategoryId() + " não encontrada."));
        User user = new User();
        user.setName(dto.name());
        user.setMiddleName(dto.middleName());
        user.setBirthday(dto.birthday());
        user.setCpf(dto.cpf());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setUserCategory(category);
        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void putUser(Long id, PutUserDTO userDTO) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Usuario com ID " + id + " não encontrado"));
        if (userRepository.existsByEmailAndIdNot(userDTO.email(), id)) {
            throw new DataConflictException("Esse e-mail já está sendo usado por outro usuário");
        }
        if (userRepository.existsByCpfAndIdNot(userDTO.cpf(), id)) {
            throw new DataConflictException("Esse CPF ja esta cadastrado");
        }
        user.updateData(userDTO);
    }


    @Transactional
    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com ID " + id + " não encontrado"));
        userRepository.delete(user);
    }

    // ----------------------------------------------------
    // NOVOS MÉTODOS PARA O MVC (CRUD DIRETO)
    // ----------------------------------------------------

    // Expõe o PasswordEncoder para que o Controller MVC possa criptografar senhas.
    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    //Retorna todos os usuarios para o controller de MVC
    public Page<User> findPageableUsers(UserFilter filter, Pageable pageable) {
        Specification<User> spec = UserSpecification.withFilter(filter);
        return userRepository.findAll(spec, pageable);
    }


     //Retorna um usuário por ID (Entidade User) para edição ou detalhes. (ControllerMVC)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado."));
    }

    //Salva ou atualiza uma entidade User diretamente (para uso do formulário MVC).
    @Transactional
    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public User save(User user) {

        String emailLimpo = user.getEmail() != null ? user.getEmail().trim() : null;
        String cpfLimpo = user.getCpf() != null ? user.getCpf().trim() : null;
        user.setEmail(emailLimpo);
        user.setCpf(cpfLimpo);

        String senhaExistenteHash;

        if (user.getId() == null) {
            senhaExistenteHash = user.getPassword();

            //VALIDAÇÃO DE CRIAÇÃO: E-MAIL e CPF devem ser únicos.
            if (userRepository.existsByEmail(emailLimpo)) {
                throw new DataConflictException("Esse e-mail já está sendo usado por outro usuário");
            }
            if (userRepository.existsByCpf(cpfLimpo)) {
                throw new DataConflictException("Esse CPF já está cadastrado");
            }

        } else {

            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + user.getId() + " não encontrado para atualização."));
            senhaExistenteHash = existingUser.getPassword();

            // VALIDAÇÃO DE EDIÇÃO: E-MAIL e CPF devem ser únicos, exceto para o ID atual.
            if (userRepository.existsByEmailAndIdNot(emailLimpo, user.getId())) {
                throw new DataConflictException("Esse e-mail já está sendo usado por outro usuário");
            }
            if (userRepository.existsByCpfAndIdNot(cpfLimpo, user.getId())) {
                throw new DataConflictException("Esse CPF já está cadastrado");
            }

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(senhaExistenteHash);
            }
        }

        return userRepository.save(user);
    }
    public User findAndPrepareForEdit(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado."));
        user.setPassword(null);
        return user;
    }


}