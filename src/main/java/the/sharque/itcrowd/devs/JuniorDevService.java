package the.sharque.itcrowd.devs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.DigestUtils;
import the.sharque.itcrowd.chat.ChatService;
import the.sharque.itcrowd.git.GitProject;
import the.sharque.itcrowd.git.GitRepository;
import the.sharque.itcrowd.git.GitService;
import the.sharque.itcrowd.git.GitStatus;
import the.sharque.itcrowd.settings.SettingsService;

@Slf4j
@RequiredArgsConstructor
public abstract class JuniorDevService {

    public static final String AUTHOR = "Moss";
    protected static final String FINISHED = "I finished with %s nothing special just %d new methods";
    protected static final String FILE_PROBLEM = "Something wrong in file %s";
    protected static final String METHOD_PROBLEM = "Something wrong in method %s";

    public static final String START_OPTIMIZE = "Hmmm... I think I can optimize this method %s";
    public static final String BAD_OPTIMIZE = "Something went wrong with %s, I think better rollback all this shit-code";
    public static final String GOOD_OPTIMIZE = "All good, I've finished this task with %s";

    public static final Pattern PARSE_RESPONSE = Pattern.compile(
            "(?<text>.*)```.*?\\n(?<code>.*)```.*?\\n*(?<comment>.*)", Pattern.DOTALL);

    public static final String ITCRWD = "ITCRWD-";

    private final OllamaApi ollamaApi;
    private final GitService gitService;
    protected final SettingsService settingsService;
    protected final ChatService chatService;
    protected final GitRepository gitRepository;
    protected final FunctionRepository functionRepository;

    public void getToWork(LanguageType languageType, String type, String author) {
        functionRepository.findByStatusAndLanguage(FunctionStatus.NEW, languageType).ifPresent(methodObject -> {
            methodObject.setStatus(FunctionStatus.IN_PROGRESS);
            methodObject.setLastModified(LocalDateTime.now());
            functionRepository.save(methodObject);

            if (methodObject.getOriginalBody().lines().count() < 5) {
                methodObject.setStatus(FunctionStatus.SKIPPED);
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
                        methodObject.setStatus(FunctionStatus.OPTIMIZED);
                    } else {
                        methodObject.setStatus(FunctionStatus.SAME);
                    }

                    methodObject.setLastModified(LocalDateTime.now());

                    log.info("Method optimized {}", methodObject.getMethodName());
                    chatService.writeToChat(author, GOOD_OPTIMIZE.formatted(methodObject.getMethodName()));

                    functionRepository.save(methodObject);
                } else {
                    methodObject.setStatus(FunctionStatus.FAILED);
                    methodObject.setLastModified(LocalDateTime.now());

                    log.info("Method optimization failed {}", methodObject.getMethodName());
                    chatService.writeToChat(author, BAD_OPTIMIZE.formatted(methodObject.getMethodName()));

                    functionRepository.save(methodObject);
                }
            }
        });
    }

    private String askJunior(FunctionModel functionModel, String type) {
        String modelName = settingsService.getValue("Model", "deepseek-coder-v2");
        ChatRequest request = ChatRequest.builder(modelName)
                .withStream(false)
                .withMessages(List.of(
                        Message.builder(Role.SYSTEM).withContent(type).build(),
                        Message.builder(Role.USER).withContent(functionModel.getOriginalBody()).build()))
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

    @Scheduled(fixedDelay = 10000)
    public void pushChanges() {
        functionRepository.findByStatus(FunctionStatus.OPTIMIZED).ifPresent(methodObject -> {
            methodObject.setStatus(FunctionStatus.IN_PROGRESS);
            functionRepository.save(methodObject);

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

                            methodObject.setStatus(FunctionStatus.PUSHED);
                        } else {
                            methodObject.setStatus(FunctionStatus.SAME);
                        }

                        gitService.checkOutBranch(gitProject, oldBranch);
                        gitService.removeBranch(gitProject, branchName);
                    } catch (IOException e) {
                        methodObject.setStatus(FunctionStatus.FAILED);
                        functionRepository.save(methodObject);
                        throw new RuntimeException(e);
                    }

                    gitProject.setStatus(GitStatus.READY);
                    gitRepository.save(gitProject);
                }
            });
            functionRepository.save(methodObject);
        });
    }

    public void loadProject(LanguageType language) {
        FunctionStatus newFunctions = "active".equals(settingsService.getValue(AUTHOR, null))
                ? FunctionStatus.NEW
                : FunctionStatus.WAIT;

        gitRepository.findOneByStatus(GitStatus.READY).ifPresent(gitProject -> {
            gitProject.setStatus(GitStatus.IN_PROGRESS);
            gitRepository.save(gitProject);

            List<String> files = findFiles(gitProject);

            List<FunctionModel> methods = files.stream()
                    .flatMap(fileName -> detectFunctions(fileName).entrySet().stream()
                            .map(stringStringEntry -> FunctionModel.builder()
                                    .gitId(gitProject.getId())
                                    .language(language)
                                    .status(newFunctions)
                                    .fileLocation(fileName)
                                    .methodName(stringStringEntry.getKey())
                                    .originalBody(stringStringEntry.getValue())
                                    .hash(DigestUtils.md5DigestAsHex(stringStringEntry.getValue().getBytes()))
                                    .build())
                            .filter(javaMethod -> !functionRepository.existsByGitIdAndMethodNameAndHash(
                                    gitProject.getId(), javaMethod.getMethodName(), javaMethod.getHash())))
                    .toList();

            if (!methods.isEmpty()) {
                chatService.writeToChat(AUTHOR, FINISHED.formatted(gitProject.getName(), methods.size()));
                functionRepository.saveAll(methods);
            }

            gitProject.setStatus(GitStatus.READY);
            gitRepository.save(gitProject);
        });
    }

    protected abstract List<String> findFiles(GitProject gitProject);

    protected List<String> findAllFiles(GitProject gitProject, String fileExtension) {
        File directory = new File(gitProject.getLocation());
        List<String> files = Collections.emptyList();

        if (directory.exists()) {
            try (Stream<Path> stream = Files.walk(directory.toPath())) {
                files = stream.map(Path::toString)
                        .filter(string -> string.endsWith(fileExtension))
                        .peek(fileName -> log.debug("Found file {}", fileName))
                        .toList();
            } catch (IOException e) {
                chatService.writeToChat(AUTHOR, FILE_PROBLEM.formatted(e.getMessage()));
                throw new RuntimeException(e);
            }
        }

        return files;
    }

    protected abstract Map<String, String> detectFunctions(String fileName);

    public List<FunctionModel> getFunctions() {
        return functionRepository.findAllOrderByLastModifiedDesc();
    }
}
