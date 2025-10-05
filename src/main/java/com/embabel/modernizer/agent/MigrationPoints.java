package com.embabel.modernizer.agent;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * Collection of identified migration points with overview (AI-generated, not persisted)
 */
public record MigrationPoints(
        List<MigrationPointDto> migrationPoints,
        @JsonPropertyDescription("High-level overview of the migration points")
        String overview
) {
}
