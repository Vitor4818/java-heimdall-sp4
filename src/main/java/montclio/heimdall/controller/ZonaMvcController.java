package montclio.heimdall.controller;

import montclio.heimdall.exception.DataConflictException;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.model.Zona;
import montclio.heimdall.service.ZonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/zonas")
public class ZonaMvcController {

    @Autowired
    private ZonaService zonaService;

    // READ
    @GetMapping
    public String listarTodas(Model model) {
        model.addAttribute("listaZonas", zonaService.findAll());
        return "zonas/zonas_list";
    }

    // UPDATE
    @GetMapping("/edit/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Zona zona = zonaService.findById(id);
            model.addAttribute("zona", zona);
            model.addAttribute("pageTitle", "Editar Zona (ID: " + id + ")");
            return "zonas/zona_form";
        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/zonas";
        }
    }

    // UPDATE
    @PostMapping("/save")
    public String salvarOuAtualizarZona(Zona zona, Model model, RedirectAttributes ra) {
        String viewDeErro = "zonas/zona_form";
        if (zona.getId() == null) {
            ra.addFlashAttribute("error", "Erro: A criação de novas zonas não é permitida. Apenas atualização de zonas existentes.");
            return "redirect:/zonas";
        }
        try {
            zonaService.save(zona);
            ra.addFlashAttribute("message", "Zona atualizada com sucesso!");
            return "redirect:/zonas";
        } catch (DataConflictException | ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("zona", zona);
            model.addAttribute("pageTitle", "Editar Zona (ID: " + zona.getId() + ")");
            return viewDeErro;
        } catch (Exception e) {
            model.addAttribute("error", "Erro interno ao salvar a Zona.");
            model.addAttribute("zona", zona);
            model.addAttribute("pageTitle", "Editar Zona (ID: " + zona.getId() + ")");
            return viewDeErro;
        }
    }
}