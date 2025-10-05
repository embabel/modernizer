package com.embabel.modernizer.agent;

/**
 * Represents a classification for code modernization.
 *
 * @param id          unique id--should be human-readable
 * @param description
 */
public record MigrationRecipe(
        String id,
        String description
        // TODO resources (links to articles, documentation, etc.
) {
}
