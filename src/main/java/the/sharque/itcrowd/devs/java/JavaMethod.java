package the.sharque.itcrowd.devs.java;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import the.sharque.itcrowd.devs.MethodObject;
import the.sharque.itcrowd.devs.MethodsStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("JAVA_METHODS")
public class JavaMethod implements MethodObject {

    @Id
    private Long id;
    private Long gitId;
    private String fileLocation;
    private String methodName;
    private String originalBody;
    private String hash;
    private String modifiedBody;
    private String commitMessage;

    private MethodsStatus status;
    @Builder.Default
    private LocalDateTime lastModified = LocalDateTime.now();
}
