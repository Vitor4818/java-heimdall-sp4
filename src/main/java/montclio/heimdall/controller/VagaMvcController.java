package montclio.heimdall.controller;

import montclio.heimdall.dto.VagaDTO.VagaFilter;
import montclio.heimdall.exception.DataConflictException;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.model.Vaga;
import montclio.heimdall.model.Zona;
import montclio.heimdall.repository.ZonaRepository;
import montclio.heimdall.service.VagaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/vagas")
public class VagaMvcController {

    @Autowired private VagaService vagaService;
    @Autowired private ZonaRepository zonaRepository;

    @ModelAttribute("allZonas")
    public List<Zona> populateZonas() {
        return zonaRepository.findAll();
    }

    // READ
    @GetMapping
    public String listarTodas(
            Model model,
            VagaFilter filter,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Vaga> vagaPage = vagaService.findPageableVagas(filter, pageable);
        model.addAttribute("vagaPage", vagaPage);
        model.addAttribute("listaVagas", vagaPage.getContent());
        model.addAttribute("filter", filter);
        return "vagas/vagas_list";
    }

    // CREATE
    @GetMapping("/new")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("vaga", new Vaga());
        model.addAttribute("pageTitle", "Cadastrar Nova Vaga");
        return "vagas/vaga_form";
    }

    // UPDATE
    @GetMapping("/edit/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Vaga vaga = vagaService.findAndPrepareForEdit(id);
            model.addAttribute("vaga", vaga);
            model.addAttribute("pageTitle", "Editar Vaga (ID: " + id + ")");

            return "vagas/vaga_form";

        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());

            return "redirect:/vagas";

        }
    }

    // CREATE
    @PostMapping("/save")
    public String salvarOuAtualizarVaga(Vaga vaga, Model model, RedirectAttributes ra) {

        String viewDeErro = "vagas/vaga_form";

        try {
            vagaService.save(vaga);
            ra.addFlashAttribute("message", "Vaga salva com sucesso!");

            return "redirect:/vagas";

        } catch (DataConflictException | ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vaga", vaga);
            model.addAttribute("pageTitle", (vaga.getId() == null) ? "Cadastrar Nova Vaga" : "Editar Vaga (ID: " + vaga.getId() + ")");
            model.addAttribute("allZonas", populateZonas());

            return viewDeErro;
        }
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String deletarVaga(@PathVariable Long id, RedirectAttributes ra) {
        try {
            vagaService.delete(id);
            ra.addFlashAttribute("message", "Vaga deletada com sucesso!");
        } catch (ResourceNotFoundException | DataConflictException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vagas";
    }
}