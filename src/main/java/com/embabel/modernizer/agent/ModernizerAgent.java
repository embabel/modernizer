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
public class ModernizerAgent {

    private final Logger logger = LoggerFactory.getLogger(ModernizerAgent.class);

    @Action
    public Domain.MigrationPoints migrationPoints(
            Domain.MigrationTask migrationTask,
            Ai ai) throws Exception {
        var softwareProject = migrationTask.softwareProject();
        var migrationPoints = ai
                .withLlmByRole("analyzer")
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("find_migration_points")
                .createObject(
                        Domain.MigrationPoints.class,
                        Map.of(
                                "notes", migrationTask.notes(),
                                "classifications", migrationTask.classifications())
                );
        logger.info("Migration points found: \n{}",
                new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(migrationPoints));
        return migrationPoints;
    }

    @AchievesGoal(description = "Modernize codebase")
    @Action
    public Domain.MigrationsReport modernize(
            Domain.MigrationTask migrationTask,
            Domain.MigrationPoints migrationPoints,
            OperationContext context
    ) {
        var softwareProject = migrationTask.softwareProject();
        var migrations = new LinkedList<Domain.MigrationReport>();
        for (var classification : migrationTask.classifications()) {
            logger.info("Processing classification: {} - {}", classification.name(), classification.description());

            var originalBranch = softwareProject.currentBranch();
            var branchName = context.getAgentProcess().getId() + "_" + classification.name().toLowerCase();
            var success = softwareProject.createAndCheckoutBranch(branchName);
            logger.info("Classification branch created: {} - {}", branchName, success);
            migrations.addAll(context.parallelMap(
                    migrationPoints.migrationPoints()
                            .stream()
                            .filter(mp -> Objects.equals(mp.classificationName(), classification.name())).toList(),
                    1,
                    mp -> tryToFixIndividualMigrationPoint(
                            migrationTask,
                            mp, context.ai())
            ));
            // Go back to the original branch
            softwareProject.checkoutBranch(originalBranch);
        }
        return new Domain.MigrationsReport(migrations);
    }

    /**
     * Try to fix an individual migration point
     * Commit if successful, otherwise revert
     */
    private Domain.MigrationReport tryToFixIndividualMigrationPoint(
            Domain.MigrationTask migrationTask,
            Domain.MigrationPoint migrationPoint,
            Ai ai) {
        var softwareProject = migrationTask.softwareProject();
        var migrationReport = ai
                .withLlmByRole("best")
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("fix_migration_point")
                .createObject(
                        Domain.MigrationReport.class,
                        Map.of(
                                "migrationPoint", migrationPoint
                        )
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
