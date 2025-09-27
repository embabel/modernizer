package com.embabel.modernizer.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Null;

import java.util.List;

public abstract class Domain {

    // TODO would include notes and config etc
    public record Project(String root) {
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

