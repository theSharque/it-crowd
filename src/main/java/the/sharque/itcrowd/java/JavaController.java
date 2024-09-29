package the.sharque.itcrowd.java;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class JavaController {

    private final JavaService javaService;

    @GetMapping("/java")
    public String index(Model model) {
        model.addAttribute("methods", javaService.getMethods());
        return "java_list";
    }
}
