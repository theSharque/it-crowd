package the.sharque.itcrowd.ollama.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PullModel {

    private String name;
    private Boolean stream;
}
