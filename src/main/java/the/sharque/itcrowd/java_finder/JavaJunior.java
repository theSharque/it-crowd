package the.sharque.itcrowd.java_finder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JavaJunior {

    private static final Pattern PARSE_RESPONSE = Pattern.compile(
            "(?<text>.*)```.*?\\n(?<code>.*)```.*?\\n*(?<comment>.*)", Pattern.DOTALL);

    private final JavaMethodsRepository javaMethodsRepository;
    private final OllamaApi ollamaApi;

    public JavaJunior(JavaMethodsRepository javaMethodsRepository, OllamaApi ollamaApi) {
        this.javaMethodsRepository = javaMethodsRepository;
        this.ollamaApi = ollamaApi;
    }

    @Scheduled(fixedDelay = 10000)
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

            String answer = askJunior(javaMethod);
            if (answer != null) {
                Matcher matcher = PARSE_RESPONSE.matcher(answer);
                if (matcher.find()) {
                    javaMethod.setModifiedBody(matcher.group("code"));
                    javaMethod.setCommitMessage(matcher.group("comment"));
                    javaMethod.setStatus(JavaMethodsStatus.CHECKED);
                    javaMethod.setLastModified(LocalDateTime.now());

                    log.info("Method optimized {}", javaMethod.getMethodName());

                    javaMethodsRepository.save(javaMethod);
                } else {
                    javaMethod.setStatus(JavaMethodsStatus.FAILED);
                    javaMethod.setLastModified(LocalDateTime.now());

                    log.info("Method optimization failed {}", javaMethod.getMethodName());

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
