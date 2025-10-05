package com.embabel.modernizer.entity;

import com.embabel.modernizer.agent.MigrationPointDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Null;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class MigrationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private MigrationPoint migrationPoint;

    // Transient field for AI-generated DTO data
    @Transient
    private MigrationPointDto migrationPointDto;

    @Column(nullable = false)
    private boolean success;

    @Column(columnDefinition = "CLOB")
    private String notes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Null
    private String branch;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MigrationReport() {
    }

    public MigrationReport(boolean success, String notes, String branch) {
        this.success = success;
        this.notes = notes;
        this.branch = branch;
    }

    // Constructor for AI-generated report with DTO
    public MigrationReport(MigrationPointDto migrationPointDto, boolean success, String notes, String branch) {
        this.migrationPointDto = migrationPointDto;
        this.success = success;
        this.notes = notes;
        this.branch = branch;
    }

    public MigrationReport withBranch(String branch) {
        MigrationReport report = new MigrationReport(this.migrationPointDto, this.success, this.notes, branch);
        report.setMigrationPoint(this.migrationPoint);
        return report;
    }

    public MigrationPointDto migrationPointDto() {
        return migrationPointDto;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MigrationPoint getMigrationPoint() {
        return migrationPoint;
    }

    public void setMigrationPoint(MigrationPoint migrationPoint) {
        this.migrationPoint = migrationPoint;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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
}
