package montclio.heimdall.controller;

import montclio.heimdall.model.User;
import montclio.heimdall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String dashboard(Model model) {
        return "dashboard";
    }
}