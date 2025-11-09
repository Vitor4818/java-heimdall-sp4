package montclio.heimdall.controller;

import montclio.heimdall.model.User;
import montclio.heimdall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewMyProfile(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Meu Perfil");
        // Reutiliza a view de detalhes que você já tem!
        return "users/user_details";
    }

    }
