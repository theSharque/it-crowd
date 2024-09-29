package the.sharque.itcrowd.java;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JavaMethodsRepository extends CrudRepository<JavaMethod, Long> {

    boolean existsByGitIdAndMethodNameAndHash(Long id, String key, String hash);

    @Query("SELECT * FROM JAVA_METHODS WHERE STATUS = :status ORDER BY LAST_MODIFIED LIMIT 1")
    Optional<JavaMethod> findByStatus(JavaMethodsStatus status);

    @Query("SELECT * FROM JAVA_METHODS ORDER BY LAST_MODIFIED DESC")
    List<JavaMethod> findAllOrderByLastModifiedDesc();
}
