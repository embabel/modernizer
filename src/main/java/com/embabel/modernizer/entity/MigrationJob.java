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

    @ManyToOne
    @JoinColumn(name = "cookbook_id")
    private MigrationCookbook cookbook;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "migrationJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private MigrationPoints migrationPoints;

    @OneToOne(mappedBy = "migrationJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private MigrationsReport migrationsReport;

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

    public MigrationPoints getMigrationPoints() {
        return migrationPoints;
    }

    public void setMigrationPoints(MigrationPoints migrationPoints) {
        this.migrationPoints = migrationPoints;
        if (migrationPoints != null) {
            migrationPoints.setMigrationJob(this);
        }
    }

    public MigrationsReport getMigrationsReport() {
        return migrationsReport;
    }

    public void setMigrationsReport(MigrationsReport migrationsReport) {
        this.migrationsReport = migrationsReport;
        if (migrationsReport != null) {
            migrationsReport.setMigrationJob(this);
        }
    }

    // Lazy getter for transient field
    public SoftwareProject softwareProject() {
        if (softwareProject == null && projectRoot != null) {
            softwareProject = new SoftwareProject(projectRoot);
        }
        return softwareProject;
    }
}
