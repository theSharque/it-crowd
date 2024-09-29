package the.sharque.itcrowd.git;

import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitRepository extends CrudRepository<GitProject, Long> {

    @Query("SELECT * FROM GITS WHERE STATUS = :gitStatus ORDER BY LAST_MODIFIED LIMIT 1")
    Optional<GitProject> findOneByStatus(GitStatus gitStatus);
}
