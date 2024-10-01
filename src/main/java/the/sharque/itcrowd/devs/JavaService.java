package the.sharque.itcrowd.devs;

import static the.sharque.itcrowd.devs.LanguageType.JAVA;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
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
public class JavaService extends JuniorDevService {

    private final LanguageType MODULE_LANGUAGE = JAVA;
    public static final Map<String, String> JAVA_REQUEST = Map.of(
            "English", "You are Java developer, optimize the method",
            "Russian", "Ты разработчик на Java, оптимизируй этот метод"
    );

    private final Pattern HAS_CLASS = Pattern.compile(
            "package\\b\\s(?<package>.*?);.*?class.+?(?<name>.+?)\\b.*?\\{(?<body>.+)}.*", Pattern.DOTALL);
    private final Pattern METHODS = Pattern.compile(
            "(?:(?:public|private|protected|static|final|native|synchronized|abstract|transient)+\\s+)+[,.$_\\w<>\\[\\]\\s]*\\s+(?<name>[$_\\w]+\\([^)]*\\)?)\\s*\\{?[^}]*}?",
            Pattern.DOTALL);
    private final Pattern CLEAN_BODY = Pattern.compile("(?<body>.*}\\n?)");

    public JavaService(OllamaApi ollamaApi, GitService gitService, SettingsService settingsService,
            ChatService chatService,
            GitRepository gitRepository, FunctionRepository functionRepository) {
        super(ollamaApi, gitService, settingsService, chatService, gitRepository, functionRepository);
    }

    @Scheduled(fixedDelay = 60000)
    public void checkBody() {
        String lang = settingsService.getValue("Language", "English");
        getToWork(MODULE_LANGUAGE, JAVA_REQUEST.get(lang), AUTHOR);
    }

    @Scheduled(fixedDelay = 10000)
    public void loadProject() {
        loadProject(MODULE_LANGUAGE);
    }

    protected List<String> findFiles(GitProject gitProject) {
        return findAllFiles(gitProject, ".java");
    }

    protected Map<String, String> detectFunctions(String fileName) {
        File file = new File(fileName);
        Map<String, String> result = new HashMap<>();

        if (file.exists() && file.isFile() && file.canRead()) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                String content = String.join("\n", lines);
                Matcher classMatcher = HAS_CLASS.matcher(content);

                if (!classMatcher.find() || classMatcher.group("name").isBlank()) {
                    return Collections.emptyMap();
                }

                String className = classMatcher.group("package") + "." + classMatcher.group("name");
                String classBody = classMatcher.group("body");

                Matcher methodMatcher = METHODS.matcher(classBody);

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
                        result.put(className + "::" + methodName, cleanBody(methodBody.toString()));
                    }

                    methodName = nextMethodName;
                    methodBody = new StringBuilder(lines.get(i) + "\n");
                    i++;
                }

                if (methodBody != null) {
                    result.put(className + "::" + methodName, cleanBody(methodBody.toString()));
                }

                log.debug("Class {} has {} methods", className, result.size());
            } catch (IOException e) {
                chatService.writeToChat(AUTHOR, METHOD_PROBLEM.formatted(e.getMessage()));
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    private String cleanBody(String methodBody) {
        Matcher cleanBody = CLEAN_BODY.matcher(methodBody);
        if (cleanBody.find()) {
            return cleanBody.group("body");
        }

        return methodBody;
    }
}
