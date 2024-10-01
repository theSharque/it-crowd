package the.sharque.itcrowd.git;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import the.sharque.itcrowd.chat.ChatService;
import the.sharque.itcrowd.settings.SettingsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitService {

    public static final String AUTHOR = "Roy";
    private static final String GIT_HOME = "./gits/";
    private static final String NEW_PROJECT = "Looks like a new project %s";
    private static final String CLONED_WE_HAVE_IT_NOW = "New project %s cloned, we have it now.";
    private static final String PROJECT_TROUBLE = "Project %s is in trouble, check this out: %s";
    private static final String UPDATE_PROJECT = "Looking for update of project %s";
    private static final String PROJECT_UPDATED = "Project %s updated.";
    private static final String PROJECT_PROBLEM = "Project %s is in trouble, check this out: %s";
    private static final String WE_ARE_HOMELESS_NOW = "Looks like we are homeless now";

    private final GitRepository gitRepository;
    private final ChatService chatService;
    private final SettingsService settingsService;

    public void addProject(GitProject gitProject) {
        if ("active".equals(settingsService.getValue(AUTHOR, null))) {
            gitProject.setStatus(GitStatus.NEW);
        } else {
            gitProject.setStatus(GitStatus.WAIT);
        }

        gitRepository.save(gitProject);
    }

    @Scheduled(fixedRate = 5000)
    public void checkNewProject() {
        gitRepository.findOneByStatus(GitStatus.NEW).ifPresent(gitProject -> {
            gitProject.setStatus(GitStatus.IN_PROGRESS);
            gitProject.setLastModified(LocalDateTime.now());
            chatService.writeToChat(AUTHOR, NEW_PROJECT.formatted(gitProject.getName()));
            gitRepository.save(gitProject);

            gitProject = cloneNewGit(gitProject);

            gitProject.setStatus(GitStatus.READY);
            gitProject.setLastModified(LocalDateTime.now());
            chatService.writeToChat(AUTHOR, CLONED_WE_HAVE_IT_NOW.formatted(gitProject.getName()));
            gitRepository.save(gitProject);
        });
    }

    public GitProject cloneNewGit(GitProject gitProject) {
        createHome();

        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(gitProject.getUsername(),
                gitProject.getPassword());

        try (Git git = Git.cloneRepository()
                .setURI(gitProject.getUrl())
                .setCredentialsProvider(credentialsProvider)
                .setDirectory(new File(GIT_HOME + gitProject.getName()))
                .call()) {
            log.info("Cloned git repository {} with {}", gitProject.getName(),
                    git.getRepository().resolve("HEAD").getName());

            gitProject.setLocation(GIT_HOME + gitProject.getName());
            gitProject.setHash(git.getRepository().resolve("HEAD").getName());
            gitRepository.save(gitProject);

            return gitProject;
        } catch (JGitInternalException | GitAPIException | IOException e) {
            gitProject.setStatus(GitStatus.FAILED);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);
            chatService.writeToChat(AUTHOR, PROJECT_TROUBLE.formatted(gitProject.getName(), e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkUpdateProject() {
        gitRepository.findOneByStatus(GitStatus.READY).ifPresent(gitProject -> {
            gitProject.setStatus(GitStatus.IN_PROGRESS);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);

            if (gitProject.getLastModified().isAfter(LocalDateTime.now().plusHours(1))) {
                chatService.writeToChat(AUTHOR, UPDATE_PROJECT.formatted(gitProject.getName()));
                gitProject = updateGit(gitProject);
                chatService.writeToChat(AUTHOR, PROJECT_UPDATED.formatted(gitProject.getName()));
            }

            gitProject.setStatus(GitStatus.READY);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);
        });
    }

    public GitProject updateGit(GitProject gitProject) {
        createHome();
        gitProject.setLocation(GIT_HOME + gitProject.getName());

        try (Git git = Git.open(new File(gitProject.getLocation()))) {
            git.pull();

            return gitProject;
        } catch (IOException e) {
            gitProject.setStatus(GitStatus.FAILED);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);
            chatService.writeToChat(AUTHOR, PROJECT_PROBLEM.formatted(gitProject.getName(), e.getMessage()));
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

    public void resetStatus(Long id) {
        gitRepository.findById(id).ifPresent(gitProject -> {
            if (gitProject.getStatus() == GitStatus.WAIT
                    || gitProject.getStatus() == GitStatus.IN_PROGRESS
                    || gitProject.getStatus() == GitStatus.FAILED) {
                gitProject.setStatus(GitStatus.NEW);
            }

            gitRepository.save(gitProject);
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
                chatService.writeToChat(AUTHOR, WE_ARE_HOMELESS_NOW);
                log.error("Failed to create git home directory");
            }
        }
    }

    public String createBranch(GitProject gitProject, String branchName) {
        try (Git git = Git.open(new File(gitProject.getLocation()))) {
            String oldBranch = git.getRepository().getBranch();

            removeBranch(gitProject, branchName);
            git.checkout().setName(branchName).setCreateBranch(true).call();

            return oldBranch;
        } catch (GitAPIException | IOException e) {
            gitProject.setStatus(GitStatus.FAILED);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);
            throw new RuntimeException(e);
        }
    }

    public void removeBranch(GitProject gitProject, String branchName) {
        try (Git git = Git.open(new File(gitProject.getLocation()))) {
            if (git.branchList().call().stream().anyMatch(ref -> ref.getName().endsWith(branchName))) {
                git.branchDelete().setBranchNames(branchName).setForce(true).call();
            }
        } catch (GitAPIException | IOException e) {
            gitProject.setStatus(GitStatus.FAILED);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);
            throw new RuntimeException(e);
        }
    }

    public void commitChanges(GitProject gitProject, String comment) {
        try (Git git = Git.open(new File(gitProject.getLocation()))) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage(comment).call();
        } catch (GitAPIException | IOException e) {
            gitProject.setStatus(GitStatus.FAILED);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);
            throw new RuntimeException(e);
        }
    }

    public void pushBranch(GitProject gitProject) {
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(gitProject.getUsername(),
                gitProject.getPassword());

        try (Git git = Git.open(new File(gitProject.getLocation()))) {
            git.push().setCredentialsProvider(credentialsProvider).call();
        } catch (GitAPIException | IOException e) {
            gitProject.setStatus(GitStatus.FAILED);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);
            throw new RuntimeException(e);
        }
    }

    public void checkOutBranch(GitProject gitProject, String branchName) {
        try (Git git = Git.open(new File(gitProject.getLocation()))) {
            git.checkout().setName(branchName).call();
        } catch (GitAPIException | IOException e) {
            gitProject.setStatus(GitStatus.FAILED);
            gitProject.setLastModified(LocalDateTime.now());
            gitRepository.save(gitProject);
            throw new RuntimeException(e);
        }
    }
}
