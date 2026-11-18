
package com.neopadma.rabbitmqdemo;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processing_jobs")
public class TaskJob {

    @Id
    private String id;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "text")
    private String result;

    @Column(nullable = false)
    private Instant createdAt;

    protected TaskJob() {
    }

    public TaskJob(String id, String text) {
        this.id = id;
        this.text = text;
        this.status = "QUEUED";
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
