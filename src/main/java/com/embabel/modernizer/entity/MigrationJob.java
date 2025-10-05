package com.embabel.modernizer.entity;

import com.embabel.agent.domain.library.code.SoftwareProject;
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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "cookbook_id")
    private MigrationCookbook cookbook;

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

    // Transient field - not persisted
    @Transient
    private SoftwareProject softwareProject;

    public MigrationJob() {
    }

    public MigrationJob(String jobId, String projectRoot, String notes, MigrationCookbook cookbook) {
        this.jobId = jobId;
        this.projectRoot = projectRoot;
        this.notes = notes;
        this.cookbook = cookbook;
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

    public MigrationCookbook getCookbook() {
        return cookbook;
    }

    public void setCookbook(MigrationCookbook cookbook) {
        this.cookbook = cookbook;
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

    // Lazy getter for transient field
    public SoftwareProject softwareProject() {
        if (softwareProject == null && projectRoot != null) {
            softwareProject = new SoftwareProject(projectRoot);
        }
        return softwareProject;
    }
}
