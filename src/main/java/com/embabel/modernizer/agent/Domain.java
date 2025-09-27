package com.embabel.modernizer.agent;

import java.util.List;

public abstract class Domain {

    // TODO would include notes and config etc
    public record Project(String root) {
    }

    public record MigrationPossibility(String filePath) {
    }

    public record MigrationPoint(String filePath, String description) {

    }

    public record MigrationPoints(
            List<MigrationPoint> migrationPoints
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

