package com.embabel.modernizer.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.domain.library.code.SoftwareProject;
import com.embabel.coding.tools.bash.BashTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Agent(description = "Code modernization agent")
public class ModernizerAgent {

    private final Logger logger = LoggerFactory.getLogger(ModernizerAgent.class);

    private final List<Classification> classifications = List.of(
            new Classification("Legacy", "Code that is outdated and may not follow current best practices."),
            new Classification("Deprecated", "Code that uses deprecated libraries or frameworks that are no longer supported."),
            new Classification("Persistence", "Code related to persistence usage"),
            new Classification("Security", "Code related to security")
    );


    private SoftwareProject softwareProject = new SoftwareProject(
            "/Users/rjohnson/dev/qct-reference-app1"
    );

    @Action
    public Domain.MigrationPoints migrationPoints(
            UserInput userInput,
            Ai ai) throws Exception {
        var migrationPoints = ai
                .withLlmByRole("best")
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("find_migration_points")
                .createObject(
                        Domain.MigrationPoints.class,
                        Map.of(
                                "focus", userInput.getContent(),
                                "classifications", classifications)
                );
        logger.info("Migration points found: \n{}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(migrationPoints));
        return migrationPoints;
    }

    @AchievesGoal(description = "Modernize codebase")
    @Action
    public Domain.MigrationsReport modernize(
            Domain.MigrationPoints migrationPoints,
            OperationContext context
    ) {
        var migrations = new LinkedList<Domain.MigrationReport>();
        for (var classification : classifications) {
            logger.info("Processing classification: {} - {}", classification.name(), classification.description());

            var currentBranch = softwareProject.currentBranch();
            var branchName = context.getAgentProcess().getId() + "_" + classification.name().toLowerCase();
            var success = softwareProject.createAndCheckoutBranch(branchName);
            logger.info("Classification branch created: {} - {}", branchName, success);
            migrations.addAll(context.parallelMap(
                    migrationPoints.migrationPoints()
                            .stream()
                            .filter(mp -> Objects.equals(mp.classificationName(), classification.name())).toList(),
                    1,
                    mp -> tryToFixIndividualMigrationPoint(mp, context.ai())
            ));
            softwareProject.checkoutBranch(currentBranch);
        }
        return new Domain.MigrationsReport(migrations);
    }

    /**
     * Try to fix an individual migration point
     * Commit if successful, otherwise revert
     */
    private Domain.MigrationReport tryToFixIndividualMigrationPoint(
            Domain.MigrationPoint migrationPoint,
            Ai ai) {
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
            softwareProject.commit("Fix: " + migrationPoint.description(), false);
        } else {
            logger.info("Reverting branch as migration was not successful");
            softwareProject.revert();
        }
        return migrationReport.withBranch(softwareProject.currentBranch());
    }
}
