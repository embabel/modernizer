package com.embabel.modernizer.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of migration recipes (immutable entity)
 */
@Entity
@Table(name = "migration_cookbook")
public class MigrationCookbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "cookbook_recipe",
            joinColumns = @JoinColumn(name = "cookbook_id"),
            inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    private List<MigrationRecipe> recipes = new ArrayList<>();

    protected MigrationCookbook() {
    }

    public MigrationCookbook(String name, List<MigrationRecipe> recipes) {
        this.name = name;
        this.recipes = new ArrayList<>(recipes);
    }

    public MigrationCookbook(String name, MigrationRecipe... recipes) {
        this.name = name;
        this.recipes = new ArrayList<>(List.of(recipes));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<MigrationRecipe> getRecipes() {
        return recipes;
    }

    // Static cookbook definitions
    public static final MigrationCookbook MODERNIZE_JAVA = new MigrationCookbook(
            "MODERNIZE_JAVA",
            new MigrationRecipe("Legacy", "Code that is outdated and may not follow current best practices."),
            new MigrationRecipe("Deprecated", "Code that uses deprecated libraries or frameworks that are no longer supported."),
            new MigrationRecipe("Persistence", "Code related to persistence usage"),
            new MigrationRecipe("Security", "Code related to security")
    );
}
