package montclio.heimdall.controller;

import montclio.heimdall.dto.TagRfidDTO.TagRfidFilter;
import montclio.heimdall.model.Motorcycle;
import montclio.heimdall.model.TagRfId;
import montclio.heimdall.repository.MotorcycleRepository;
import montclio.heimdall.service.TagRfidService;
import montclio.heimdall.exception.ResourceNotFoundException;
import montclio.heimdall.exception.DataConflictException;
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
@RequestMapping("/tags") // Rota base para o CRUD MVC
public class TagRfidMvcController {

    @Autowired private TagRfidService tagRfidService;
    @Autowired private MotorcycleRepository motorcycleRepository; // Para o dropdown

    @ModelAttribute("allMotorcycles")
    public List<Motorcycle> populateMotorcycles() {
        return motorcycleRepository.findAll();
    }

    // READ
    @GetMapping
    public String listarTodas( Model model, TagRfidFilter filter, @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable )
    {
        Page<TagRfId> tagPage = tagRfidService.findAllPageable(filter, pageable);
        model.addAttribute("tagPage", tagPage); // Objeto Page completo para a paginação
        model.addAttribute("listaTags", tagPage.getContent()); // Para o loop th:each
        model.addAttribute("filter", filter); // Para manter os valores no formulário de busca
        return "tags/tags_list";
    }

    @GetMapping("/view/{id}")
    public String viewTagById(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            TagRfId tag = tagRfidService.findById(id);
            model.addAttribute("tag", tag);
            model.addAttribute("pageTitle", "Detalhes da Tag (ID: " + id + ")");
            return "tags/tag_details"; // View: tags/tag_details.html

        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/tags";
        }
    }

    // CREATE
    @GetMapping("/new")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("tag", new TagRfId());
        model.addAttribute("pageTitle", "Cadastrar Nova Tag RFID");
        return "tags/tag_form";
    }

    // UPDATE
    @GetMapping("/edit/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            TagRfId tag = tagRfidService.findById(id);
            model.addAttribute("tag", tag);
            model.addAttribute("pageTitle", "Editar Tag (ID: " + id + ")");
            return "tags/tag_form";
        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", "Tag não encontrada: " + e.getMessage());
            return "redirect:/tags";
        }
    }

    // CREATE
    @PostMapping("/save")
    public String salvarOuAtualizarTag(TagRfId tag, Model model, RedirectAttributes ra) {
        String urlDeErro = (tag.getId() == null) ? "tags/tag_form" : "tags/tag_form"; // Retorno direto da View

        try {
            tagRfidService.save(tag);
            ra.addFlashAttribute("message", "Tag salva e associada com sucesso!");
            return "redirect:/tags";

        } catch (DataConflictException | ResourceNotFoundException e) {
            model.addAttribute("error", "Erro de Validação: " + e.getMessage());
            model.addAttribute("tag", tag);
            model.addAttribute("pageTitle", (tag.getId() == null) ? "Cadastrar Nova Tag RFID" : "Editar Tag (ID: " + tag.getId() + ")");

            return urlDeErro;

        } catch (Exception e) {
            String erroMsg = e.getMessage().contains("ORA-00001") ?
                    "Erro de Banco: Violação de Unicidade de Moto. Verifique se a moto já tem uma tag." :
                    "Erro interno: " + e.getMessage();

            model.addAttribute("error", erroMsg);
            model.addAttribute("tag", tag);
            model.addAttribute("pageTitle", (tag.getId() == null) ? "Cadastrar Nova Tag RFID" : "Editar Tag (ID: " + tag.getId() + ")");

            return urlDeErro;
        }
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String deletarTag(@PathVariable Long id, RedirectAttributes ra) {
        try {
            tagRfidService.deleteTag(id);
            ra.addFlashAttribute("message", "Tag deletada e desvinculada com sucesso!");
        } catch (ResourceNotFoundException e) {
            ra.addFlashAttribute("error", "Erro ao deletar: " + e.getMessage());
        }
        return "redirect:/tags";
    }
}