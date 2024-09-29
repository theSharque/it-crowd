package the.sharque.itcrowd.language;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface MethodRepository<T> extends CrudRepository<T, Long> {

    boolean notExistsByGitIdAndMethodNameAndHash(Long id, String key, String hash);

    Optional<T> findByStatus(MethodsStatus status);

    List<T> findAllOrderByLastModifiedDesc();
}
