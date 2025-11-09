package montclio.heimdall.controller;

import montclio.heimdall.model.User;
import montclio.heimdall.model.UserCategory;
import montclio.heimdall.repository.UserCategoryRepository;
import montclio.heimdall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicController {

    @Autowired private UserService userService;
    @Autowired private UserCategoryRepository userCategoryRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String exibirFormularioRegistro(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Vamos criar este HTML
    }

    @PostMapping("/register")
    public String processarRegistro(User user, RedirectAttributes ra) {
        try {
            UserCategory defaultRole = userCategoryRepository.findAll().stream()
                    .filter(c -> c.getCategory().equalsIgnoreCase("USER")) // ou "OPERADOR"
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Role padrão não encontrada no banco."));

            user.setUserCategory(defaultRole);

            userService.save(user);

            ra.addFlashAttribute("message", "Cadastro realizado com sucesso! Faça login.");
            return "redirect:/login";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erro ao cadastrar: " + e.getMessage());
            return "redirect:/register";
        }
    }
}