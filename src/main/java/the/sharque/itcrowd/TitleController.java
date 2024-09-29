package the.sharque.itcrowd;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TitleController {

    @GetMapping
    public String title() {
        return "title";
    }
}
