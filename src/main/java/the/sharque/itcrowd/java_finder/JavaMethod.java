package the.sharque.itcrowd.java_finder;

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
@Table("JAVA_METHODS")
public class JavaMethod {

    @Id
    private Long id;
    private Long gitId;
    private String methodName;
    private String originalBody;
    private String hash;
    private String modifiedBody;
    private String commitMessage;

    private JavaMethodsStatus status;
    private LocalDateTime lastModified;
}
