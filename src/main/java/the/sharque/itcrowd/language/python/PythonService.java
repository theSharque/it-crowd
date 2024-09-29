package the.sharque.itcrowd.language.python;

import static the.sharque.itcrowd.language.JuniorDev.PYTHON_REQUEST;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import the.sharque.itcrowd.chat.ChatService;
import the.sharque.itcrowd.git.GitProject;
import the.sharque.itcrowd.git.GitRepository;
import the.sharque.itcrowd.git.GitStatus;
import the.sharque.itcrowd.language.JuniorDev;
import the.sharque.itcrowd.language.MethodsStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class PythonService {

    public static final String AUTHOR = "Moss";
    private static final String FINISHED = "I finished with %s nothing special just %d new methods";
    private static final String FILE_PROBLEM = "Something wrong in file %s";
    private static final String METHOD_PROBLEM = "Something wrong in method %s";

    private final Pattern FUNCTION = Pattern.compile(".*?def +(?<name>.*?\\)) *?:", Pattern.DOTALL);
    private final GitRepository gitRepository;
    private final PythonMethodsRepository pythonMethodsRepository;
    private final ChatService chatService;
    private final JuniorDev juniorDev;

    @Scheduled(fixedDelay = 10000)
    public void loadMethods() {
        gitRepository.findOneByStatus(GitStatus.CLONED).ifPresent(gitProject -> {
            gitProject.setStatus(GitStatus.IN_PROGRESS);
            gitRepository.save(gitProject);

            List<String> files = findPythonFunctions(gitProject);

            List<PythonMethod> methods = files.stream()
                    .map(this::findMethods)
                    .flatMap(stringStringMap -> stringStringMap.entrySet().stream())
                    .map(stringStringEntry -> PythonMethod.builder()
                            .gitId(gitProject.getId())
                            .status(MethodsStatus.NEW)
                            .methodName(stringStringEntry.getKey())
                            .originalBody(stringStringEntry.getValue())
                            .hash(DigestUtils.md5DigestAsHex(stringStringEntry.getValue().getBytes()).toUpperCase())
                            .build())
                    .filter(javaMethod -> pythonMethodsRepository.notExistsByGitIdAndMethodNameAndHash(
                            gitProject.getId(), javaMethod.getMethodName(), javaMethod.getHash()))
                    .toList();

            if (!methods.isEmpty()) {
                chatService.writeToChat(AUTHOR, FINISHED.formatted(gitProject.getName(), methods.size()));
                pythonMethodsRepository.saveAll(methods);
            }

            gitProject.setStatus(GitStatus.CLONED);
            gitRepository.save(gitProject);
        });
    }

    public List<String> findPythonFunctions(GitProject gitProject) {
        File directory = new File(gitProject.getLocation());
        List<String> files = Collections.emptyList();

        if (directory.exists()) {
            try (Stream<Path> stream = Files.walk(directory.toPath())) {
                files = stream.map(Path::toString)
                        .filter(string -> string.endsWith(".py"))
                        .peek(fileName -> log.info("Found file {}", fileName))
                        .toList();
            } catch (IOException e) {
                chatService.writeToChat(AUTHOR, FILE_PROBLEM.formatted(e.getMessage()));
                throw new RuntimeException(e);
            }
        }

        return files;
    }

    public Map<String, String> findMethods(String fileName) {
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
                        result.put(fileName + "." + methodName, methodBody.toString());
                    }

                    methodName = nextMethodName;
                    methodBody = new StringBuilder(lines.get(i) + "\n");
                    i++;
                }

                if (methodBody != null) {
                    result.put(fileName + "." + methodName, methodBody.toString());
                }

                log.info("File {} has {} Functions", fileName, result.size());
            } catch (IOException e) {
                chatService.writeToChat(AUTHOR, METHOD_PROBLEM.formatted(e.getMessage()));
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    public List<PythonMethod> getFunctions() {
        return pythonMethodsRepository.findAllOrderByLastModifiedDesc();
    }

    @Scheduled(fixedDelay = 60000)
    public void checkBody() {
        juniorDev.getToWork(pythonMethodsRepository, PYTHON_REQUEST);
    }
}
