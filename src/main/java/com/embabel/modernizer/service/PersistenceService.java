package com.embabel.modernizer.service;

import com.embabel.modernizer.entity.MigrationCookbook;
import com.embabel.modernizer.entity.MigrationJob;
import com.embabel.modernizer.entity.MigrationPoints;
import com.embabel.modernizer.repository.MigrationCookbookRepository;
import com.embabel.modernizer.repository.MigrationJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Transactional service. We need this distinct from the agent
 * because we don't want transactions to span LLM calls.
 */
@Service
@Transactional
public class PersistenceService {

    private final MigrationJobRepository migrationJobRepository;
    private final MigrationCookbookRepository migrationCookbookRepository;

    public PersistenceService(
            MigrationJobRepository migrationJobRepository,
            MigrationCookbookRepository migrationCookbookRepository) {
        this.migrationJobRepository = migrationJobRepository;
        this.migrationCookbookRepository = migrationCookbookRepository;
    }

    /**
     * Save or update a migration job
     */
    public MigrationJob saveMigrationJob(MigrationJob migrationJob) {
        // Ensure cookbook is managed/persistent
        if (migrationJob.getCookbook() != null) {
            var cookbook = migrationJob.getCookbook();
            var existingCookbook = migrationCookbookRepository.findByName(cookbook.getName());
            if (existingCookbook.isPresent()) {
                migrationJob.setCookbook(existingCookbook.get());
            } else {
                // Save cookbook first if it's new
                var savedCookbook = migrationCookbookRepository.save(cookbook);
                migrationJob.setCookbook(savedCookbook);
            }
        }
        return migrationJobRepository.save(migrationJob);
    }

    /**
     * Get or create a cookbook by name
     */
    public MigrationCookbook getOrCreateCookbook(MigrationCookbook cookbook) {
        return migrationCookbookRepository.findByName(cookbook.getName())
                .orElseGet(() -> migrationCookbookRepository.save(cookbook));
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
    public MigrationPoints saveMigrationPoints(MigrationPoints migrationPoints) {
        return migrationJobRepository.save(migrationPoints.getMigrationJob())
                .getMigrationPoints();
    }

    /**
     * Get all migration jobs
     */
    @Transactional(readOnly = true)
    public List<MigrationJob> findAllMigrationJobs() {
        return migrationJobRepository.findAll();
    }

}
