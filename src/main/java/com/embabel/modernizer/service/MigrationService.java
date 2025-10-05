package com.embabel.modernizer.service;

import com.embabel.modernizer.agent.MigrationPoints;
import com.embabel.modernizer.entity.MigrationJob;
import com.embabel.modernizer.entity.MigrationReport;
import com.embabel.modernizer.repository.MigrationJobRepository;
import com.embabel.modernizer.repository.MigrationPointRepository;
import com.embabel.modernizer.repository.MigrationReportJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MigrationService {

    private final MigrationJobRepository migrationJobRepository;
    private final MigrationPointRepository migrationPointRepository;
    private final MigrationReportJpaRepository migrationReportRepository;

    public MigrationService(
            MigrationJobRepository migrationJobRepository,
            MigrationPointRepository migrationPointRepository,
            MigrationReportJpaRepository migrationReportRepository) {
        this.migrationJobRepository = migrationJobRepository;
        this.migrationPointRepository = migrationPointRepository;
        this.migrationReportRepository = migrationReportRepository;
    }

    /**
     * Save or update a migration job
     */
    public MigrationJob saveMigrationJob(MigrationJob migrationJob) {
        return migrationJobRepository.save(migrationJob);
    }

    /**
     * Find migration job by job ID
     */
    public Optional<MigrationJob> findMigrationJobByJobId(String jobId) {
        return migrationJobRepository.findByJobId(jobId);
    }

    /**
     * Save migration points from AI analysis
     */
    public com.embabel.modernizer.entity.MigrationPoints saveMigrationPoints(
            MigrationPoints dto,
            MigrationJob migrationJob) {
        var entity = com.embabel.modernizer.entity.MigrationPoints.fromDto(dto, migrationJob);
        migrationJob.addMigrationPointsSet(entity);
        migrationJobRepository.save(migrationJob);
        return entity;
    }

    /**
     * Save a migration report
     */
    public MigrationReport saveMigrationReport(MigrationReport report) {
        return migrationReportRepository.save(report);
    }

    /**
     * Save multiple migration reports
     */
    public List<MigrationReport> saveMigrationReports(List<MigrationReport> reports) {
        return migrationReportRepository.saveAll(reports);
    }

    /**
     * Get all migration jobs
     */
    @Transactional(readOnly = true)
    public List<MigrationJob> findAllMigrationJobs() {
        return migrationJobRepository.findAll();
    }

    /**
     * Get migration points for a job
     */
    @Transactional(readOnly = true)
    public List<com.embabel.modernizer.entity.MigrationPoint> findMigrationPointsByJobId(Long jobId) {
        return migrationPointRepository.findByMigrationJobId(jobId);
    }

    /**
     * Get migration reports for a migration point
     */
    @Transactional(readOnly = true)
    public List<MigrationReport> findMigrationReportsByPointId(Long pointId) {
        return migrationReportRepository.findByMigrationPointId(pointId);
    }
}
