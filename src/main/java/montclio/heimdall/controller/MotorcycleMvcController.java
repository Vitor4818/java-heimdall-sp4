package montclio.heimdall.controller;

import montclio.heimdall.model.Motorcycle;
import montclio.heimdall.model.Vaga;
import montclio.heimdall.model.TagRfId;
import montclio.heimdall.service.MotorcycleService;
import montclio.heimdall.repository.VagaRepository;
import montclio.heimdall.repository.TagRfidRepository;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.exception.DataConflictException;
import montclio.heimdall.dto.MotorcycleDTO.MotorcycleFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/motorcycles")
public class MotorcycleMvcController {

    @Autowired private MotorcycleService motorcycleService;
    @Autowired private VagaRepository vagaRepository;
    @Autowired private TagRfidRepository tagRfidRepository;

    @ModelAttribute("allVagas")
    public List<Vaga> populateVagas() { return vagaRepository.findAll(); }
    @ModelAttribute("allTags")
    public List<TagRfId> populateTags() { return tagRfidRepository.findAll(); }


    // READ
    @GetMapping
    public String listarTodos(
            Model model,
            MotorcycleFilter filter,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Motorcycle> motoPage = motorcycleService.findAllPageable(filter, pageable);

        model.addAttribute("motoPage", motoPage);
        model.addAttribute("listaMotos", motoPage.getContent());
        model.addAttribute("filter", filter);

        return "motos/motos_list";
    }

    // CREATE
    @GetMapping("/new")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("motorcycle", new Motorcycle());
        model.addAttribute("pageTitle", "Cadastrar Nova Moto");
        return "motos/moto_form";
    }

    // UPDATE
    @GetMapping("/edit/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Motorcycle moto = motorcycleService.findById(id);
            model.addAttribute("motorcycle", moto);
            model.addAttribute("pageTitle", "Editar Moto (ID: " + id + ")");
            return "motos/moto_form";

        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/motorcycles";
        }
    }

    // 4. CREATE / UPDATE
    @PostMapping("/save")
    public String salvarOuAtualizarMoto(Motorcycle moto, Model model, RedirectAttributes ra) {
        String viewDeErro = "motos/moto_form";
        try {
            motorcycleService.save(moto);
            ra.addFlashAttribute("message", "Moto e Status de Vaga atualizados com sucesso!");
            return "redirect:/motorcycles";

        } catch (DataConflictException | ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("motorcycle", moto);
            model.addAttribute("pageTitle", (moto.getId() == null) ? "Cadastrar Nova Moto" : "Editar Moto (ID: " + moto.getId() + ")");
            model.addAttribute("allVagas", populateVagas());
            model.addAttribute("allTags", populateTags());
            return viewDeErro;
        }
    }

    // 5. DELETE
    @GetMapping("/delete/{id}")
    public String deletarMotorcycle(@PathVariable Long id, RedirectAttributes ra) {
        try {
            motorcycleService.delete(id);
            ra.addFlashAttribute("message", "Moto deletada e vaga liberada com sucesso!");
        } catch (ResourceNotFoundException | DataConflictException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/motorcycles";
    }
}