package the.sharque.itcrowd.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String CHAT_MESSAGE = "%s [%s] %s";
    private final ChatRepository chatRepository;

    public void writeToChat(String author, String message) {
        chatRepository.save(ChatMessage.builder()
                .author(author)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<String> getLast(Long limit) {
        return StreamSupport.stream(chatRepository.findLastChatMessages(limit).spliterator(), false)
                .map(chatMessage -> CHAT_MESSAGE.formatted(chatMessage.getTimestamp().toString(),
                        chatMessage.getAuthor(), chatMessage.getMessage()))
                .toList();
    }
}
