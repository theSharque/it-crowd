package the.sharque.itcrowd.language;

import java.time.LocalDateTime;

public interface MethodObject {

    void setOriginalBody(String originalBody);

    void setStatus(MethodsStatus status);

    void setModifiedBody(String modifiedBody);

    void setCommitMessage(String commitMessage);

    void setLastModified(LocalDateTime lastModified);

    String getOriginalBody();

    String getHash();

    String getModifiedBody();

    String getCommitMessage();

    MethodsStatus getStatus();

    LocalDateTime getLastModified();

    String getMethodName();

    Long getId();

    Long getGitId();

    String getFileLocation();
}


