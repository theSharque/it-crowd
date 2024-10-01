package the.sharque.itcrowd.devs;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionRepository extends CrudRepository<FunctionModel, Long> {

    boolean existsByGitIdAndMethodNameAndHash(Long id, String key, String hash);

    @Query("SELECT * FROM FUNCTIONS WHERE STATUS = :status ORDER BY LAST_MODIFIED LIMIT 1")
    Optional<FunctionModel> findByStatus(FunctionStatus status);

    @Query("SELECT * FROM FUNCTIONS ORDER BY LAST_MODIFIED")
    List<FunctionModel> findAllOrderByLastModifiedDesc();

    List<FunctionModel> findAllByLanguage(LanguageType language);
}
