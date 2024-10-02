package the.sharque.itcrowd.ollama.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelDetail {

    private String format;
    private String family;
    private List<String> families;
    private String parameterSize;
    private String quantizationLevel;
}
