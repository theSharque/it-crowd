package the.sharque.itcrowd.language;

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

    private final OllamaApi ollamaApi;
    private final ChatService chatService;

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
                    methodObject.setStatus(MethodsStatus.OPTIMIZED);
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
                        Message.builder(Role.SYSTEM)
                                .withContent(type)
                                .build(),
                        Message.builder(Role.USER)
                                .withContent(methodObject.getOriginalBody())
                                .build()))
                .withOptions(OllamaOptions.create().withTemperature(0.5f))
                .build();

        ChatResponse response = ollamaApi.chat(request);

        if (response.done()) {
            log.trace("Response done reason {}", response.doneReason());
            log.trace("Response text {}", response.message().content());

            return response.message().content();
        }

        return null;
    }
}
