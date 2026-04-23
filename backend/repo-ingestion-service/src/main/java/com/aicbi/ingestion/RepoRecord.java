package com.aicbi.ingestion;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class RepoRecord {
    @Id
    private String id;
    private String repoUrl;
    private String localPath;
    private Instant createdAt;

    public RepoRecord() {}
    public RepoRecord(String id, String repoUrl, String localPath, Instant createdAt) {
        this.id = id; this.repoUrl = repoUrl; this.localPath = localPath; this.createdAt = createdAt;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
