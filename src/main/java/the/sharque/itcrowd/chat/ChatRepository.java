package the.sharque.itcrowd.chat;

import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends CrudRepository<ChatMessage, Long> {

    @Query("SELECT * FROM CHAT ORDER BY TIMESTAMP DESC LIMIT :limit")
    Iterable<ChatMessage> findLastChatMessages(Long limit);

    @Query("SELECT * FROM CHAT WHERE AUTHOR = :author ORDER BY TIMESTAMP DESC LIMIT 1")
    Optional<ChatMessage> findOneByAuthor(String author);
}
