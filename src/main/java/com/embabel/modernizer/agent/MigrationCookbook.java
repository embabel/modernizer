package com.embabel.modernizer.agent;

import java.util.List;

/**
 * List of focus areas
 *
 * @param recipes
 */
public record MigrationCookbook(
        List<MigrationRecipe> recipes
) {

    public MigrationCookbook(MigrationRecipe... migrationRecipes) {
        this(List.of(migrationRecipes));
    }

    public static MigrationCookbook MODERNIZE_JAVA = new MigrationCookbook(
            new MigrationRecipe("Legacy", "Code that is outdated and may not follow current best practices."),
            new MigrationRecipe("Deprecated", "Code that uses deprecated libraries or frameworks that are no longer supported."),
            new MigrationRecipe("Persistence", "Code related to persistence usage"),
            new MigrationRecipe("Security", "Code related to security")
    );
}
