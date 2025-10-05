package com.embabel.modernizer.agent;

/**
 * DTO for migration point identification from AI (not persisted)
 */
public record MigrationPointDto(
        String filePath,
        String description,
        String recipeId) {
}
