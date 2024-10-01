package the.sharque.itcrowd.devs;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("FUNCTIONS")
public class FunctionModel {

    @Id
    private Long id;
    private Long gitId;
    private LanguageType language;
    private String fileLocation;
    private String methodName;
    private String originalBody;
    private String hash;
    private String modifiedBody;
    private String commitMessage;

    private FunctionStatus status;
    @Builder.Default
    private LocalDateTime lastModified = LocalDateTime.now();
}


