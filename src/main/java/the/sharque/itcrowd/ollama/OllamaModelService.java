package the.sharque.itcrowd.ollama;

import static the.sharque.itcrowd.devs.JuniorDevService.AUTHOR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import the.sharque.itcrowd.chat.ChatService;
import the.sharque.itcrowd.ollama.models.ModelData;
import the.sharque.itcrowd.ollama.models.ModelList;
import the.sharque.itcrowd.ollama.models.PullModel;
import the.sharque.itcrowd.settings.SettingsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaModelService {

    private static final String PULL_MODEL = "I need to pull model, please wait...";
    private static final String MODEL_PULLED = "Model pulled I'm ready to work with code now";

    private final SettingsService settingsService;
    private final ChatService chatService;
    public final AtomicBoolean ollamaDisabled = new AtomicBoolean(true);

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaServerUrl;

    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient client = HttpClient.newHttpClient();

    @Scheduled(fixedDelay = 10000)
    public void checkOllamaModel() {
        String modelName = settingsService.getValue("Model", "deepseek-coder-v2");

        ModelData model = getModelList()
                .getModels().stream()
                .filter(modelData -> modelData.getName().startsWith(modelName))
                .findAny()
                .orElse(null);

        if (model == null) {
            chatService.writeToChat(AUTHOR, PULL_MODEL);
            log.info("Pull model {}", modelName);
            pullModel(modelName);
            chatService.writeToChat(AUTHOR, MODEL_PULLED);
        } else {
            log.debug("Model {} found", modelName);
            ollamaDisabled.set(false);
        }
    }

    public ModelList getModelList() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ollamaServerUrl + "/api/tags")).GET().build();

        try {
            String response = client.send(request, BodyHandlers.ofString()).body();
            return mapper.readerFor(ModelList.class).readValue(response);
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage());
        }

        return new ModelList();
    }

    public void pullModel(String modelName) {
        PullModel pullModel = new PullModel(modelName, false);

        try {
            String postData = mapper.writerFor(PullModel.class).writeValueAsString(pullModel);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(ollamaServerUrl + "/api/pull"))
                    .POST(BodyPublishers.ofString(postData)).build();

            if (client.send(request, BodyHandlers.ofString()).statusCode() == 200) {
                log.info("Pull Model successful");
            } else {
                log.error("Pull Model failed");
            }
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage());
        }
    }
}
