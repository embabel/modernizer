package com.embabel.modernizer.entity;

import com.embabel.modernizer.agent.MigrationPointDto;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class MigrationPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_job_id", nullable = false)
    private MigrationJob migrationJob;

    @Column(columnDefinition = "CLOB")
    private String overview;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    // Note: migration points themselves are already stored in MigrationPoint entity
    // This entity just stores the overview and links to the job

    public MigrationPoints() {
    }

    public MigrationPoints(MigrationJob migrationJob, String overview) {
        this.migrationJob = migrationJob;
        this.overview = overview;
    }

    /**
     * Construct from DTO and create MigrationPoint entities
     */
    public static MigrationPoints fromDto(com.embabel.modernizer.agent.MigrationPoints dto, MigrationJob migrationJob) {
        MigrationPoints entity = new MigrationPoints(migrationJob, dto.overview());

        // Create MigrationPoint entities from DTOs
        for (MigrationPointDto pointDto : dto.migrationPoints()) {
            MigrationPoint point = new MigrationPoint(
                pointDto.filePath(),
                pointDto.description(),
                pointDto.recipeId()
            );
            migrationJob.addMigrationPoint(point);
        }

        return entity;
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
}
