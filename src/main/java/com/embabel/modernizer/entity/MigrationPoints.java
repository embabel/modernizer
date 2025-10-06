package com.embabel.modernizer.entity;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class MigrationPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_job_id", nullable = false)
    private MigrationJob migrationJob;

    @OneToMany(mappedBy = "migrationPoints", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MigrationPoint> migrationPoints = new ArrayList<>();

    @JsonPropertyDescription("High-level overview of the migration points")
    @Column(columnDefinition = "CLOB")
    private String overview;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public MigrationPoints() {
    }

    public MigrationPoints(List<MigrationPoint> migrationPoints, String overview) {
        this.migrationPoints = migrationPoints;
        this.overview = overview;
        // Set back-reference
        for (MigrationPoint point : migrationPoints) {
            point.setMigrationPoints(this);
        }
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

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
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

    public void addMigrationPoint(MigrationPoint migrationPoint) {
        migrationPoints.add(migrationPoint);
        migrationPoint.setMigrationPoints(this);
    }
}
