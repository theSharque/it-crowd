package the.sharque.itcrowd.git;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitService {

    private static final String GIT_HOME = "./gits/";
    private final GitRepository gitRepository;

    public void addProject(GitProject gitProject) {
        gitProject.setStatus(GitStatus.NEW);
        gitRepository.save(gitProject);
    }

    @Scheduled(fixedRate = 5000)
    public void checkProject() {
        gitRepository.findOneByStatus(GitStatus.NEW).ifPresent(gitProject -> {
            gitProject.setStatus(GitStatus.IN_PROGRESS);
            gitRepository.save(gitProject);

            gitProject = cloneNewGit(gitProject);

            gitProject.setStatus(GitStatus.CLONED);
            gitRepository.save(gitProject);
        });
    }

    public GitProject cloneNewGit(GitProject gitProject) {
        createHome();

        try (Git git = Git.cloneRepository()
                .setURI(gitProject.getUrl())
                .setDirectory(new File(GIT_HOME + gitProject.getName()))
                .call()) {
            log.info("Cloned git repository {} with {}", gitProject.getName(),
                    git.getRepository().resolve("HEAD").getName());

            gitProject.setLocation(GIT_HOME + gitProject.getName());
            gitProject.setHash(git.getRepository().resolve("HEAD").getName());
            gitRepository.save(gitProject);

            return gitProject;
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GitProject updateGit(GitProject gitProject) {
        createHome();
        gitProject.setLocation(GIT_HOME + gitProject.getName());

        try (Git git = Git.open(new File(gitProject.getLocation()))) {
            git.pull();

            return gitProject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<GitProject> getProjects() {
        return StreamSupport.stream(gitRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(GitProject::getName))
                .toList();
    }

    public void deleteProject(Long id) {
        gitRepository.findById(id).ifPresent(gitProject -> {
            gitProject.setStatus(GitStatus.IN_PROGRESS);
            File directoryToBeDeleted = new File(GIT_HOME + gitProject.getName());

            if (deleteDirectory(directoryToBeDeleted)) {
                gitRepository.deleteById(id);
            }
        });
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private void createHome() {
        File home = new File(GIT_HOME);
        if (!home.exists()) {
            if (!home.mkdirs()) {
                log.error("Failed to create git home directory");
            }
        }
    }
}
