package the.sharque.itcrowd.devs.python;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PythonController {

    private final PythonService pythonService;

    @GetMapping("/python")
    public String index(Model model) {
        model.addAttribute("funs", pythonService.getFunctions());
        return "python_list";
    }

    @GetMapping("/python/reset/{id}")
    public String reset(@PathVariable Long id) {
        pythonService.resetMethod(id);
        return "redirect:/python";
    }
}
