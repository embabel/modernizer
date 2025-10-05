package com.embabel.modernizer.entity;

import com.embabel.agent.domain.library.code.SoftwareProject;
import com.embabel.modernizer.agent.MigrationCookbook;
import com.embabel.modernizer.agent.MigrationRecipe;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class MigrationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String jobId;

    @Column(nullable = false, length = 1024)
    private String projectRoot;

    @Column(columnDefinition = "CLOB")
    private String notes;

    private String cookbookName;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "migrationJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MigrationPoint> migrationPoints = new ArrayList<>();

    @OneToMany(mappedBy = "migrationJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MigrationPoints> migrationPointsSets = new ArrayList<>();

    // Transient fields - not persisted
    @Transient
    private SoftwareProject softwareProject;

    @Transient
    private MigrationCookbook cookbook;

    public MigrationJob() {
    }

    public MigrationJob(String jobId, String projectRoot, String notes, String cookbookName) {
        this.jobId = jobId;
        this.projectRoot = projectRoot;
        this.notes = notes;
        this.cookbookName = cookbookName;
    }

    public MigrationJob(String jobId, String projectRoot, String notes, MigrationCookbook cookbook) {
        this.jobId = jobId;
        this.projectRoot = projectRoot;
        this.notes = notes;
        this.cookbook = cookbook;
        this.cookbookName = cookbook != null ? serializeCookbook(cookbook) : null;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCookbookName() {
        return cookbookName;
    }

    public void setCookbookName(String cookbookName) {
        this.cookbookName = cookbookName;
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

    public List<MigrationPoint> getMigrationPoints() {
        return migrationPoints;
    }

    public void setMigrationPoints(List<MigrationPoint> migrationPoints) {
        this.migrationPoints = migrationPoints;
    }

    // Helper methods for bidirectional relationship
    public void addMigrationPoint(MigrationPoint migrationPoint) {
        migrationPoints.add(migrationPoint);
        migrationPoint.setMigrationJob(this);
    }

    public void removeMigrationPoint(MigrationPoint migrationPoint) {
        migrationPoints.remove(migrationPoint);
        migrationPoint.setMigrationJob(null);
    }

    public List<MigrationPoints> getMigrationPointsSets() {
        return migrationPointsSets;
    }

    public void setMigrationPointsSets(List<MigrationPoints> migrationPointsSets) {
        this.migrationPointsSets = migrationPointsSets;
    }

    public void addMigrationPointsSet(MigrationPoints migrationPointsSet) {
        migrationPointsSets.add(migrationPointsSet);
        migrationPointsSet.setMigrationJob(this);
    }

    // Lazy getters for transient fields
    public SoftwareProject softwareProject() {
        if (softwareProject == null && projectRoot != null) {
            softwareProject = new SoftwareProject(projectRoot);
        }
        return softwareProject;
    }

    public MigrationCookbook cookbook() {
        if (cookbook == null && cookbookName != null) {
            cookbook = deserializeCookbook(cookbookName);
        }
        return cookbook;
    }

    // Serialization helpers
    private String serializeCookbook(MigrationCookbook cookbook) {
        if (cookbook == null || cookbook.recipes() == null) {
            return null;
        }
        return cookbook.recipes().stream()
                .map(MigrationRecipe::id)
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }

    private MigrationCookbook deserializeCookbook(String cookbookName) {
        // For now, return null - cookbook reconstruction would need recipe lookup
        // This could be enhanced to look up recipes by ID
        return null;
    }
}
