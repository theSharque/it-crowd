package the.sharque.itcrowd.language.python;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PythonController {

    private final PythonService javaService;

    @GetMapping("/python")
    public String index(Model model) {
        model.addAttribute("funs", javaService.getFunctions());
        return "python_list";
    }
}
