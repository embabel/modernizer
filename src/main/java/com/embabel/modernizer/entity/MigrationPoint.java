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
public class MigrationPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_points_id", nullable = false)
    private MigrationPoints migrationPoints;

    @Column(nullable = false, length = 1024)
    private String filePath;

    @Column(columnDefinition = "CLOB")
    private String description;

    private String recipeId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
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


    public Instant getUpdatedAt() {
        return updatedAt;
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

    public MigrationPoints getMigrationPoints() {
        return migrationPoints;
    }

    public void setMigrationPoints(MigrationPoints migrationPoints) {
        this.migrationPoints = migrationPoints;
    }
}
