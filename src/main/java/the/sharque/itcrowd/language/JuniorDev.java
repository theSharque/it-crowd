package the.sharque.itcrowd.language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import the.sharque.itcrowd.chat.ChatService;
import the.sharque.itcrowd.git.GitRepository;
import the.sharque.itcrowd.git.GitService;
import the.sharque.itcrowd.git.GitStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class JuniorDev {

    public static final String START_OPTIMIZE = "Hmmm... I think I can optimize this method %s";
    public static final String BAD_OPTIMIZE = "Something went wrong with %s, I think better rollback all this shit-code";
    public static final String GOOD_OPTIMIZE = "All good, I've finished this task with %s";

    public static final Pattern PARSE_RESPONSE = Pattern.compile(
            "(?<text>.*)```.*?\\n(?<code>.*)```.*?\\n*(?<comment>.*)", Pattern.DOTALL);

    public static final String JAVA_REQUEST = "You are Java developer, optimize the method";
    public static final String PYTHON_REQUEST = "You are python developer, optimize the method";
    public static final String ITCRWD = "ITCRWD-";

    private final OllamaApi ollamaApi;
    private final ChatService chatService;
    private final GitRepository gitRepository;
    private final GitService gitService;

    public <T extends MethodObject> void getToWork(MethodRepository<T> methodRepository, String type, String author) {
        methodRepository.findByStatus(MethodsStatus.NEW).ifPresent(methodObject -> {
            methodObject.setStatus(MethodsStatus.IN_PROGRESS);
            methodObject.setLastModified(LocalDateTime.now());
            methodRepository.save(methodObject);

            if (methodObject.getOriginalBody().lines().count() < 5) {
                methodObject.setStatus(MethodsStatus.SKIPPED);
                methodObject.setLastModified(LocalDateTime.now());
            }

            log.info("Ask junior to optimize the method: {}", methodObject.getMethodName());
            chatService.writeToChat(author, START_OPTIMIZE.formatted(methodObject.getMethodName()));

            String answer = askJunior(methodObject, type);
            if (answer != null) {
                Matcher matcher = PARSE_RESPONSE.matcher(answer);
                if (matcher.find()) {
                    methodObject.setModifiedBody(matcher.group("code"));
                    methodObject.setCommitMessage(matcher.group("comment"));

                    if (StringSimilarity.similarity(methodObject.getOriginalBody(),
                            methodObject.getModifiedBody()) < 0.8) {
                        methodObject.setStatus(MethodsStatus.OPTIMIZED);
                    } else {
                        methodObject.setStatus(MethodsStatus.SAME);
                    }

                    methodObject.setLastModified(LocalDateTime.now());

                    log.info("Method optimized {}", methodObject.getMethodName());
                    chatService.writeToChat(author, GOOD_OPTIMIZE.formatted(methodObject.getMethodName()));

                    methodRepository.save(methodObject);
                } else {
                    methodObject.setStatus(MethodsStatus.FAILED);
                    methodObject.setLastModified(LocalDateTime.now());

                    log.info("Method optimization failed {}", methodObject.getMethodName());
                    chatService.writeToChat(author, BAD_OPTIMIZE.formatted(methodObject.getMethodName()));

                    methodRepository.save(methodObject);
                }
            }
        });
    }

    private String askJunior(MethodObject methodObject, String type) {
        ChatRequest request = ChatRequest.builder("deepseek-coder-v2")
                .withStream(false)
                .withMessages(List.of(
                        Message.builder(Role.SYSTEM).withContent(type).build(),
                        Message.builder(Role.USER).withContent(methodObject.getOriginalBody()).build()))
                .withOptions(OllamaOptions.create().withTemperature(0.5f))
                .build();

        try {
            ChatResponse response = ollamaApi.chat(request);
            if (response.done()) {
                log.trace("Response done reason {}", response.doneReason());
                log.trace("Response text {}", response.message().content());

                return response.message().content();
            }
        } catch (RuntimeException e) {
            log.error("Ollama error {}", e.getMessage());
        }

        return null;
    }

    public <T extends MethodObject> void pushChanges(MethodRepository<T> methodRepository) {
        methodRepository.findByStatus(MethodsStatus.OPTIMIZED).ifPresent(methodObject -> {
            methodObject.setStatus(MethodsStatus.IN_PROGRESS);
            methodRepository.save(methodObject);

            gitRepository.findById(methodObject.getGitId()).ifPresent(gitProject -> {
                gitProject.setStatus(GitStatus.IN_PROGRESS);
                gitRepository.save(gitProject);

                File updateFile = new File(methodObject.getFileLocation());

                if (updateFile.exists() && updateFile.isFile() && updateFile.canRead() && updateFile.canWrite()) {
                    String branchName = ITCRWD + methodObject.getId();

                    gitService.removeBranch(gitProject, branchName);
                    String oldBranch = gitService.createBranch(gitProject, branchName);

                    try {
                        String content = Files.readString(updateFile.toPath());

                        if (content.contains(methodObject.getOriginalBody())) {
                            content = content.replace(methodObject.getOriginalBody(), methodObject.getModifiedBody());
                            Files.write(updateFile.toPath(), content.getBytes());
                            gitService.commitChanges(gitProject, methodObject.getCommitMessage());
                            gitService.pushBranch(gitProject);

                            methodObject.setStatus(MethodsStatus.PUSHED);
                        } else {
                            methodObject.setStatus(MethodsStatus.SAME);
                        }

                        gitService.checkOutBranch(gitProject, oldBranch);
                        gitService.removeBranch(gitProject, branchName);
                    } catch (IOException e) {
                        methodObject.setStatus(MethodsStatus.FAILED);
                        methodRepository.save(methodObject);
                        throw new RuntimeException(e);
                    }

                    gitProject.setStatus(GitStatus.READY);
                    gitRepository.save(gitProject);
                }
            });
            methodRepository.save(methodObject);
        });
    }
}
