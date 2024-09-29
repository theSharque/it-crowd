package the.sharque.itcrowd.chat;

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
@Table("CHAT")
public class ChatMessage {

    @Id
    private Long id;
    private String message;
    private String author;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
