package the.sharque.itcrowd.java;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import the.sharque.itcrowd.chat.ChatService;

@Slf4j
@Service
@RequiredArgsConstructor
public class JavaJunior {

    private static final String AUTHOR = "Moss";
    private static final String START_OPTIMIZE = "Hmmm... I think I can optimize this method %s";
    private static final String BAD_OPTIMIZE = "Something went wrong with %s, I think better rollback all this shit-code";
    private static final String GOOD_OPTIMIZE = "All good, I've finished this task with %s";

    private static final Pattern PARSE_RESPONSE = Pattern.compile(
            "(?<text>.*)```.*?\\n(?<code>.*)```.*?\\n*(?<comment>.*)", Pattern.DOTALL);

    private final JavaMethodsRepository javaMethodsRepository;
    private final OllamaApi ollamaApi;
    private final ChatService chatService;

    @Scheduled(fixedDelay = 60000)
    public void checkBody() {
        javaMethodsRepository.findByStatus(JavaMethodsStatus.NEW).ifPresent(javaMethod -> {
            javaMethod.setStatus(JavaMethodsStatus.IN_PROGRESS);
            javaMethod.setLastModified(LocalDateTime.now());
            javaMethodsRepository.save(javaMethod);

            if (javaMethod.getOriginalBody().lines().count() < 5) {
                javaMethod.setStatus(JavaMethodsStatus.SKIPPED);
                javaMethod.setLastModified(LocalDateTime.now());
            }

            log.info("Ask junior to optimize the method: {}", javaMethod.getMethodName());
            chatService.writeToChat(AUTHOR, START_OPTIMIZE.formatted(javaMethod.getMethodName()));

            String answer = askJunior(javaMethod);
            if (answer != null) {
                Matcher matcher = PARSE_RESPONSE.matcher(answer);
                if (matcher.find()) {
                    javaMethod.setModifiedBody(matcher.group("code"));
                    javaMethod.setCommitMessage(matcher.group("comment"));
                    javaMethod.setStatus(JavaMethodsStatus.CHECKED);
                    javaMethod.setLastModified(LocalDateTime.now());

                    log.info("Method optimized {}", javaMethod.getMethodName());
                    chatService.writeToChat(AUTHOR, GOOD_OPTIMIZE.formatted(javaMethod.getMethodName()));

                    javaMethodsRepository.save(javaMethod);
                } else {
                    javaMethod.setStatus(JavaMethodsStatus.FAILED);
                    javaMethod.setLastModified(LocalDateTime.now());

                    log.info("Method optimization failed {}", javaMethod.getMethodName());
                    chatService.writeToChat(AUTHOR, BAD_OPTIMIZE.formatted(javaMethod.getMethodName()));

                    javaMethodsRepository.save(javaMethod);
                }
            }
        });
    }

    private String askJunior(JavaMethod javaMethod) {
        ChatRequest request = ChatRequest.builder("deepseek-coder-v2")
                .withStream(false)
                .withMessages(List.of(
                        Message.builder(Role.SYSTEM)
                                .withContent("You are Java developer, optimize the code.")
                                .build(),
                        Message.builder(Role.USER)
                                .withContent(javaMethod.getOriginalBody())
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
