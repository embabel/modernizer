package com.embabel.modernizer.entity;

import jakarta.persistence.*;

/**
 * Represents a classification for code modernization (immutable entity)
 */
@Entity
@Table(name = "migration_recipe")
public class MigrationRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String recipeId;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String description;

    protected MigrationRecipe() {
    }

    public MigrationRecipe(String recipeId, String description) {
        this.recipeId = recipeId;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getDescription() {
        return description;
    }
}
