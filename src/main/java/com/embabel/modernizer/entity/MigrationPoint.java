package com.embabel.modernizer.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "migration_point")
public class MigrationPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_job_id", nullable = false)
    private MigrationJob migrationJob;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    @Column(name = "recipe_id")
    private String recipeId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "migrationPoint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MigrationReport> migrationReports = new ArrayList<>();

    public MigrationPoint() {
    }

    public MigrationPoint(String filePath, String description, String recipeId) {
        this.filePath = filePath;
        this.description = description;
        this.recipeId = recipeId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MigrationJob getMigrationJob() {
        return migrationJob;
    }

    public void setMigrationJob(MigrationJob migrationJob) {
        this.migrationJob = migrationJob;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MigrationReport> getMigrationReports() {
        return migrationReports;
    }

    public void setMigrationReports(List<MigrationReport> migrationReports) {
        this.migrationReports = migrationReports;
    }

    // Helper methods for bidirectional relationship
    public void addMigrationReport(MigrationReport migrationReport) {
        migrationReports.add(migrationReport);
        migrationReport.setMigrationPoint(this);
    }

    public void removeMigrationReport(MigrationReport migrationReport) {
        migrationReports.remove(migrationReport);
        migrationReport.setMigrationPoint(null);
    }
}
