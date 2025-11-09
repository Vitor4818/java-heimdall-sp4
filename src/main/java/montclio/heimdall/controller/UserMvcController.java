package montclio.heimdall.controller;

import montclio.heimdall.dto.UserDTO.UserFilter;
import montclio.heimdall.model.User;
import montclio.heimdall.model.UserCategory;
import montclio.heimdall.repository.UserCategoryRepository;
import montclio.heimdall.service.UserService;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.exception.DataConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserMvcController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserCategoryRepository userCategoryRepository;

    // Métodos utilitários para buscar listas (para o Thymeleaf)
    @ModelAttribute("allCategories")
    public List<UserCategory> populateCategories() {
        return userCategoryRepository.findAll();
    }




    // READ (Listagem de Usuários)
    @GetMapping
    public String listarTodos( Model model, UserFilter filter, @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<User> userPage = userService.findPageableUsers(filter, pageable);
        model.addAttribute("userPage", userPage);
        model.addAttribute("listaUsuarios", userPage.getContent());
        model.addAttribute("filter", filter);
        return "users/users_list";
    }

    // READ (Detalhes por ID)
    @GetMapping("/view/{id}")
    public String viewUserById(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            User user = userService.findById(id);
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Detalhes do Usuário (ID: " + id + ")");
            return "users/user_details";

        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage()); // Exibe a mensagem de erro que veio do Service
            return "redirect:/users";
        }
    }

    // CREATE (Exibir Formulário de Cadastro)
    @GetMapping("/new")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("pageTitle", "Cadastrar Novo Usuário");
        return "users/user_form";
    }

    // UPDATE (Exibir Formulário de Edição)
    @GetMapping("/edit/{id}")
    public String exibirFormularioEdicao(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            User user = userService.findAndPrepareForEdit(id);
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Editar Usuário (ID: " + id + ")");
            return "users/user_form";

        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/users";
        }
    }

    // CREATE / UPDATE (Processar Dados do Formulário)
    @PostMapping("/save")
    public String salvarOuAtualizarUsuario(User user, RedirectAttributes ra) {
        String urlDeErro = (user.getId() == null) ? "redirect:/users/new" : "redirect:/users/edit/" + user.getId();
        try {
            userService.save(user);
            ra.addFlashAttribute("message", "Usuário salvo com sucesso!");
            return "redirect:/users";

        } catch (DataConflictException | ResourceNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return urlDeErro;
        }
    }

    // DELETE (Exclusão)
    @GetMapping("/delete/{id}")
    public String deletarUsuario(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id); // Reutiliza o método deleteUser() robusto
            ra.addFlashAttribute("message", "Usuário deletado com sucesso!");
        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", "Erro ao deletar: " + e.getMessage());
        }
        return "redirect:/users";
    }
}