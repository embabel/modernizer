package com.embabel.modernizer.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.coding.tools.bash.BashTools;
import com.embabel.modernizer.entity.MigrationJob;
import com.embabel.modernizer.entity.MigrationReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Agent(description = "Code modernization agent")
public record ModernizerAgent(
        ModernizerConfig config
) {

    private final static Logger logger = LoggerFactory.getLogger(ModernizerAgent.class);

    @Action
    public MigrationPoints migrationPoints(
            MigrationJob migrationJob,
            Ai ai) throws Exception {
        var softwareProject = migrationJob.softwareProject();
        var migrationPoints = ai
                .withLlm(config.analyzer())
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("find_migration_points")
                .createObject(
                        MigrationPoints.class,
                        Map.of(
                                "notes", migrationJob.getNotes(),
                                "classifications", migrationJob.cookbook().recipes())
                );
        logger.info("{} migration points found: \n{}",
                migrationPoints.migrationPoints().size(),
                new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(migrationPoints));
        return migrationPoints;
    }

    @AchievesGoal(description = "Modernize codebase")
    @Action
    public List<MigrationReport> modernize(
            MigrationJob migrationJob,
            MigrationPoints migrationPoints,
            OperationContext context
    ) {
        var softwareProject = migrationJob.softwareProject();
        var migrations = new LinkedList<MigrationReport>();
        for (var classification : migrationJob.cookbook().recipes()) {
            logger.info("Processing classification: {} - {}", classification.id(), classification.description());

            var originalBranch = softwareProject.currentBranch();
            var branchName = context.getAgentProcess().getId() + "_" + classification.id().toLowerCase();
            var success = softwareProject.createAndCheckoutBranch(branchName);
            logger.info("Classification branch {} created from branch {} - {}", branchName, originalBranch, success);
            migrations.addAll(context.parallelMap(
                    migrationPoints.migrationPoints()
                            .stream()
                            .filter(mp -> Objects.equals(mp.recipeId(), classification.id())).toList(),
                    1,
                    mp -> tryToFixIndividualMigrationPoint(
                            migrationJob,
                            mp, context.ai())
            ));
            // Go back to the original branch
            logger.info("Switching back from classification branch {} to original branch {}", branchName, originalBranch);
            softwareProject.checkoutBranch(originalBranch);
        }
        return migrations;
    }

    /**
     * Try to fix an individual migration point
     * Commit if successful, otherwise revert
     */
    private MigrationReport tryToFixIndividualMigrationPoint(
            MigrationJob migrationJob,
            MigrationPointDto migrationPoint,
            Ai ai) {
        var softwareProject = migrationJob.softwareProject();
        var migrationReport = ai
                .withLlm(config.fixer())
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("fix_migration_point")
                .createObject(
                        MigrationReport.class,
                        Map.of(
                                "migrationPoint", migrationPoint
                        )
                );
        if (migrationReport.isSuccess()) {
            var message = "Fix: " + migrationPoint.description();
            softwareProject.commit(message, false);
            logger.info("Committing branch {} - {} as migration was successful",
                    softwareProject.currentBranch(), message);

        } else {
            logger.warn("Reverting branch {} as migration was not successful", softwareProject.currentBranch());
            softwareProject.revert();
        }
        return migrationReport.withBranch(softwareProject.currentBranch());
    }
}
