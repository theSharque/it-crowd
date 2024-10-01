package the.sharque.itcrowd.devs;

import static the.sharque.itcrowd.devs.LanguageType.PYTHON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import the.sharque.itcrowd.chat.ChatService;
import the.sharque.itcrowd.git.GitProject;
import the.sharque.itcrowd.git.GitRepository;
import the.sharque.itcrowd.git.GitService;
import the.sharque.itcrowd.settings.SettingsService;

@Slf4j
@Service
public class PythonService extends JuniorDevService {

    private final LanguageType MODULE_LANGUAGE = PYTHON;

    private final Pattern FUNCTION = Pattern.compile(".*?def +(?<name>.*?\\)) *?:", Pattern.DOTALL);

    public PythonService(OllamaApi ollamaApi, GitService gitService,
            SettingsService settingsService, ChatService chatService,
            GitRepository gitRepository, FunctionRepository functionRepository) {
        super(ollamaApi, gitService, settingsService, chatService, gitRepository, functionRepository);
    }


    @Scheduled(fixedDelay = 60000)
    public void checkBody() {
        getToWork(PYTHON_REQUEST, AUTHOR);
    }

    @Scheduled(fixedDelay = 10000)
    public void pushUpdate() {
        pushChanges();
    }

    @Scheduled(fixedDelay = 10000)
    public void loadProject() {
        loadProject(MODULE_LANGUAGE);
    }

    protected List<String> findFiles(GitProject gitProject) {
        return findAllFiles(gitProject, ".py");
    }

    protected Map<String, String> detectFunctions(String fileName) {
        File file = new File(fileName);
        Map<String, String> result = new HashMap<>();

        if (file.exists() && file.isFile() && file.canRead()) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                String content = String.join("\n", lines);
                Matcher methodMatcher = FUNCTION.matcher(content);

                int i = 0;
                StringBuilder methodBody = null;
                String methodName = null;
                while (methodMatcher.find()) {
                    String nextMethodName = methodMatcher.group("name");

                    while (!lines.get(i).contains(nextMethodName)) {
                        if (methodBody != null) {
                            methodBody.append(lines.get(i)).append("\n");
                        }

                        i++;
                    }

                    if (methodBody != null) {
                        result.put(fileName + "::" + methodName, methodBody.toString());
                    }

                    methodName = nextMethodName;
                    methodBody = new StringBuilder(lines.get(i) + "\n");
                    i++;
                }

                if (methodBody != null) {
                    result.put(fileName + "::" + methodName, methodBody.toString());
                }

                log.debug("File {} has {} Functions", fileName, result.size());
            } catch (IOException e) {
                chatService.writeToChat(AUTHOR, METHOD_PROBLEM.formatted(e.getMessage()));
                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
