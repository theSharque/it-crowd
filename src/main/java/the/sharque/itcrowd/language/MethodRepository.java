package the.sharque.itcrowd.language;

import java.util.List;
import java.util.Optional;

public interface MethodRepository<T> {

    boolean existsByGitIdAndMethodNameAndHash(Long id, String key, String hash);

    Optional<T> findByStatus(MethodsStatus status);

    List<T> findAllOrderByLastModifiedDesc();

    T save(T methodObject);
}
