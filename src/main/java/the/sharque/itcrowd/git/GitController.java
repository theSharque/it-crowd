package the.sharque.itcrowd.git;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import the.sharque.itcrowd.java_finder.JavaService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GitController {

    private final GitService gitService;
    private final JavaService javaService;

    @GetMapping
    public String getList(Model model) {
        model.addAttribute("projects", gitService.getProjects());
        return "projects";
    }

    @GetMapping("/add")
    public String addView(Model model) {
        return "newProject";
    }

    @PostMapping(value = "/add")
    public String addProject(@ModelAttribute GitProject gitProject) {
        gitService.addProject(gitProject);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteProject(Model model, @PathVariable Long id) {
        gitService.deleteProject(id);
        return "redirect:/";
    }

    @GetMapping("/test")
    public String test(Model model) {

        GitProject gitProject = GitProject.builder()
                .name("ostm")
                .url("https://github.com/ostm")
                .build();

        gitProject = gitService.updateGit(gitProject);
        List<String> files = javaService.findJavaFiles(gitProject);

        Map<String, String> methods = files.stream()
                .map(javaService::findMethods)
                .flatMap(stringStringMap -> stringStringMap.entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        log.info(methods.toString());
        return "test";
    }
}
