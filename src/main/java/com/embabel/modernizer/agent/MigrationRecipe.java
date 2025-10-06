package com.embabel.modernizer.agent;

/**
 * Represents a classification for code modernization.
 *
 * @param name        unique name--should be human-readable
 * @param description
 */
public record MigrationRecipe(
        String name,
        String description
        // TODO resources (links to articles, documentation, etc.
) {
}
