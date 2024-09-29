package the.sharque.itcrowd.git;

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
@Table("GITS")
public class GitProject {

    @Id
    private Long id;
    private String name;
    private String url;
    private String hash;

    private GitStatus status;
    private LocalDateTime lastModified;

    private String location;
}
