package com.embabel.modernizer.agent;

import com.embabel.agent.domain.library.code.SoftwareProject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Null;

import java.util.List;

public abstract class Domain {

    public record MigrationTask(
            String root,
            String notes,
            List<Classification> classifications
    ) {

        public MigrationTask(
                String root,
                String notes
        ) {
            this(root, notes, List.of(
                    new Classification("Legacy", "Code that is outdated and may not follow current best practices."),
                    new Classification("Deprecated", "Code that uses deprecated libraries or frameworks that are no longer supported."),
                    new Classification("Persistence", "Code related to persistence usage"),
                    new Classification("Security", "Code related to security")
            ));
        }

        public SoftwareProject softwareProject() {
            return new SoftwareProject(root);
        }
    }

    public record MigrationPossibility(String filePath) {
    }

    public record MigrationPoint(String filePath,
                                 String description,
                                 String classificationName) {

    }

    public record MigrationPoints(
            List<MigrationPoint> migrationPoints,
            @JsonPropertyDescription("High-level overview of the migration points")
            String overview
    ) {
    }

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

    public record MigrationsReport(
            List<MigrationReport> migration
    ) {
    }
}

