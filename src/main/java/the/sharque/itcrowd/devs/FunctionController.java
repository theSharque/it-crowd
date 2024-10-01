package the.sharque.itcrowd.devs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class FunctionController {

    private final FunctionService functionService;

    @GetMapping("/dev")
    public String dev() {
        return "redirect:/dev/" + LanguageType.JAVA;
    }

    @GetMapping("/dev/{language}")
    public String index(Model model, @PathVariable LanguageType language) {
        model.addAttribute("functions", functionService.getFunctions(language));
        return "function_list";
    }

    @GetMapping("/dev/reset/{id}")
    public String reset(@PathVariable Long id) {
        return functionService.resetMethod(id)
                .map(languageType -> "redirect:/dev/" + languageType.name())
                .orElse("redirect:/dev");
    }
}
