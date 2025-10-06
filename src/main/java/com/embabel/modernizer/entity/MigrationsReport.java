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
public class MigrationsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_job_id", nullable = false)
    private MigrationJob migrationJob;

    @OneToMany(mappedBy = "migrationsReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MigrationReport> migrationReports = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public MigrationsReport() {
    }

    public MigrationsReport(List<MigrationReport> migrationReports) {
        this.migrationReports = migrationReports;
        for (MigrationReport report : migrationReports) {
            report.setMigrationsReport(this);
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

    public List<MigrationReport> getMigrationReports() {
        return migrationReports;
    }

    public void setMigrationReports(List<MigrationReport> migrationReports) {
        this.migrationReports = migrationReports;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void addMigrationReport(MigrationReport migrationReport) {
        migrationReports.add(migrationReport);
        migrationReport.setMigrationsReport(this);
    }

    public String toString() {
        return "MigrationsReport{id=" + id + ", migrationJobId=" + (migrationJob != null ? migrationJob.getId() : null) +
                ", migrationReportsCount=" + migrationReports.size() + "}";
    }
}
