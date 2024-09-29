package the.sharque.itcrowd.git;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GitController {

    private final GitService gitService;

    @GetMapping("/git")
    public String getList(Model model) {
        model.addAttribute("projects", gitService.getProjects());
        return "projects";
    }

    @GetMapping("/git/add")
    public String addView() {
        return "newProject";
    }

    @PostMapping(value = "/git/add")
    public String addProject(@ModelAttribute GitProject gitProject) {
        gitService.addProject(gitProject);
        return "redirect:/git";
    }

    @GetMapping("/git/delete/{id}")
    public String deleteProject(@PathVariable Long id) {
        gitService.deleteProject(id);
        return "redirect:/git";
    }
}
