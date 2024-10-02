package the.sharque.itcrowd.ollama.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelData {

    private String name;
    private LocalDateTime modifiedAt;
    private Long size;
    private String digest;
    private ModelDetail details;
}
