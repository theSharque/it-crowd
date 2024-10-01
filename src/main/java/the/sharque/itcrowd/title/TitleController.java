package the.sharque.itcrowd.title;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import the.sharque.itcrowd.chat.ChatService;
import the.sharque.itcrowd.devs.JavaService;
import the.sharque.itcrowd.git.GitService;

@Controller
@RequiredArgsConstructor
public class TitleController {

    private final ChatService chatService;

    @GetMapping
    public String title(Model model) {
        model.addAttribute("roy", chatService.getLastMessage(GitService.AUTHOR));
        model.addAttribute("moss", chatService.getLastMessage(JavaService.AUTHOR));

        return "title";
    }
}
