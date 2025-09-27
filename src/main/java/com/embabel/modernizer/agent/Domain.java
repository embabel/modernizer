package com.embabel.modernizer.agent;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

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
            String notes
    ) {
    }

    public record MigrationsReport(
            List<MigrationReport> migration
    ) {
    }
}

