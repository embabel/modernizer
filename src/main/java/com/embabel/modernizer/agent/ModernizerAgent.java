package com.embabel.modernizer.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.coding.tools.bash.BashTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

@Agent(description = "Code modernization agent")
public record ModernizerAgent(
        ModernizerConfig config
) {

    private final static Logger logger = LoggerFactory.getLogger(ModernizerAgent.class);

    @Action
    public Domain.MigrationPoints migrationPoints(
            Domain.MigrationJob migrationJob,
            Ai ai) throws Exception {
        var softwareProject = migrationJob.softwareProject();
        var migrationPoints = ai
                .withLlm(config.analyzer())
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("find_migration_points")
                .createObject(
                        Domain.MigrationPoints.class,
                        Map.of(
                                "notes", migrationJob.notes(),
                                "recipes", migrationJob.cookbook().recipes()),
                        "find_migration_points"
                );
        logger.info("{} migration points found:\n{}",
                migrationPoints.migrationPoints().size(),
                new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(migrationPoints));
        return migrationPoints;
    }

    @AchievesGoal(description = "Modernize codebase")
    @Action
    public Domain.MigrationsReport modernize(
            Domain.MigrationJob migrationJob,
            Domain.MigrationPoints migrationPoints,
            OperationContext context
    ) {
        var softwareProject = migrationJob.softwareProject();
        var migrations = new LinkedList<Domain.MigrationReport>();
        for (var recipe : migrationJob.cookbook().recipes()) {
            var recipeHits = migrationPoints.migrationPoints()
                    .stream()
                    .filter(mp -> Objects.equals(mp.recipeName(), recipe.name())).toList();

            if (recipeHits.isEmpty()) {
                logger.info("No migration points found for recipe: {} - {}, skipping",
                        recipe.name(), recipe.description());
                continue;
            }
            logger.info("Processing recipe: {} - {}", recipe.name(), recipe.description());

            var originalBranch = softwareProject.currentBranch();
            var branchName = context.getAgentProcess().getId() + "_" + recipe.name().toLowerCase();
            var success = softwareProject.createAndCheckoutBranch(branchName);
            logger.info("Recipe branch {} created from branch {} - {}", branchName, originalBranch, success);
            migrations.addAll(context.parallelMap(
                    migrationPoints.migrationPoints()
                            .stream()
                            .filter(mp -> Objects.equals(mp.recipeName(), recipe.name())).toList(),
                    1,
                    mp -> tryToFixIndividualMigrationPoint(
                            migrationJob,
                            mp, context.ai())
            ));
            logger.info("Switching back from recipe branch {} to original branch {}", branchName, originalBranch);
            softwareProject.checkoutBranch(originalBranch);
        }
        return new Domain.MigrationsReport(migrations);
    }

    /**
     * Try to fix an individual migration point
     * Commit if successful, otherwise revert
     */
    private Domain.MigrationReport tryToFixIndividualMigrationPoint(
            Domain.MigrationJob migrationJob,
            Domain.MigrationPoint migrationPoint,
            Ai ai) {
        var softwareProject = migrationJob.softwareProject();
        var migrationReport = ai
                .withLlm(config.fixer())
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("fix_migration_point")
                .createObject(
                        Domain.MigrationReport.class,
                        Map.of(
                                "migrationPoint", migrationPoint
                        ),
                        "fix_migration_point"
                );
        if (migrationReport.success()) {
            var message = "Fix: " + migrationPoint.description();
            softwareProject.commit(message, false);
            logger.info("Committing branch {} - {} as migration was not successful",
                    softwareProject.currentBranch(), message);

        } else {
            logger.warn("Reverting branch {} as migration was not successful", softwareProject.currentBranch());
            softwareProject.revert();
        }
        return migrationReport.withBranch(softwareProject.currentBranch());
    }
}
