package the.sharque.itcrowd.language.java;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.stereotype.Repository;
import the.sharque.itcrowd.language.MethodRepository;
import the.sharque.itcrowd.language.MethodsStatus;

@Repository
public interface JavaMethodsRepository extends MethodRepository<JavaMethod> {

    boolean existsByGitIdAndMethodNameAndHash(Long id, String key, String hash);

    @Query("SELECT * FROM JAVA_METHODS WHERE STATUS = :status ORDER BY LAST_MODIFIED LIMIT 1")
    Optional<JavaMethod> findByStatus(MethodsStatus status);

    @Query("SELECT * FROM JAVA_METHODS ORDER BY LAST_MODIFIED DESC")
    List<JavaMethod> findAllOrderByLastModifiedDesc();
}
