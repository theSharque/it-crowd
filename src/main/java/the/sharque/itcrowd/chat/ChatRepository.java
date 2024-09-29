package the.sharque.itcrowd.chat;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends CrudRepository<ChatMessage, Long> {

    @Query("SELECT * FROM CHAT ORDER BY TIMESTAMP DESC LIMIT :limit")
    Iterable<ChatMessage> findLastChatMessages(Long limit);
}
