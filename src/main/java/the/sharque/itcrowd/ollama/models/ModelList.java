package the.sharque.itcrowd.ollama.models;

import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class ModelList {

    private List<ModelData> models = Collections.emptyList();
}
