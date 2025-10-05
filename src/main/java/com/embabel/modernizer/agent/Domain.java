package com.embabel.modernizer.agent;

import com.embabel.agent.domain.library.code.SoftwareProject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Null;

import java.util.List;

/**
 * Container class to avoid proliferation of files
 */
public abstract class Domain {

    /**
     * Task to migrate a project
     */
    public record MigrationTask(SoftwareProject softwareProject, String notes, TaskList tasks) {
        /**
         * Create a new project
         *
         * @param root  project root on local machine
         * @param notes notes about the task
         * @param tasks tasks to consider
         */
        public MigrationTask(String root, String notes, TaskList tasks) {
            this(new SoftwareProject(root), notes, tasks);
        }
    }

    /**
     * Identified migration point.
     *
     * @param filePath
     * @param description
     * @param classificationName
     */
    public record MigrationPoint(
            String filePath,
            String description,
            String classificationName) {
    }

    public record MigrationPoints(
            List<MigrationPoint> migrationPoints,
            @JsonPropertyDescription("High-level overview of the migration points")
            String overview
    ) {
    }

    /**
     * Report on a specific migration
     *
     * @param migrationPoint migration point
     * @param success        whether the migration was successful
     * @param notes          any notes about the migration
     * @param branch         branch where the migration was made, null if no branch has been created yet
     */
    public record MigrationReport(
            MigrationPoint migrationPoint,
            boolean success,
            String notes,
            @JsonProperty(access = JsonProperty.Access.READ_ONLY)
            @Null String branch
    ) {
        public MigrationReport withBranch(String branch) {
            return new MigrationReport(migrationPoint, success, notes, branch);
        }

    }

    /**
     * Report on migrations performed
     *
     * @param migration
     */
    public record MigrationsReport(
            List<MigrationReport> migration
    ) {
    }
}

