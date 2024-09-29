package the.sharque.itcrowd.language.python;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import the.sharque.itcrowd.language.MethodRepository;
import the.sharque.itcrowd.language.MethodsStatus;

@Repository
public interface PythonMethodsRepository extends CrudRepository<PythonMethod, Long>, MethodRepository<PythonMethod> {

    boolean existsByGitIdAndMethodNameAndHash(Long id, String key, String hash);

    @Query("SELECT * FROM PYTHON_METHODS WHERE STATUS = :status ORDER BY LAST_MODIFIED LIMIT 1")
    Optional<PythonMethod> findByStatus(MethodsStatus status);

    @Query("SELECT * FROM PYTHON_METHODS ORDER BY LAST_MODIFIED DESC")
    List<PythonMethod> findAllOrderByLastModifiedDesc();
}
