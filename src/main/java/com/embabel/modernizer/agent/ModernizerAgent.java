package com.embabel.modernizer.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.coding.tools.bash.BashTools;
import com.embabel.modernizer.entity.*;
import com.embabel.modernizer.service.PersistenceService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Agent(description = "Code modernization agent")
public record ModernizerAgent(
        ModernizerConfig config,
        PersistenceService persistenceService
) {

    private final static Logger logger = LoggerFactory.getLogger(ModernizerAgent.class);

    private record MigrationPointDto(
            String filePath,
            String description,
            String recipeId) {
    }

    private record MigrationPointsDto(
            List<MigrationPointDto> migrationPoints,
            @JsonPropertyDescription("High-level overview of the migration points")
            String overview
    ) {
    }

    private record MigrationReportDto(
            boolean success,
            String notes,
            @JsonProperty(access = JsonProperty.Access.READ_ONLY)
            @Nullable String branch
    ) {
        /**
         * Return a new instance identifying a branch that was used for this migration
         */
        public MigrationReportDto withBranch(String branch) {
            return new MigrationReportDto(success, notes, branch);
        }

    }


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
                        MigrationPointsDto.class,
                        Map.of(
                                "notes", migrationJob.getNotes(),
                                "classifications", migrationJob.getCookbook().getRecipes()),
                        "findMigrationPoints"
                );
        logger.info("{} migration points found: \n{}",
                migrationPoints.migrationPoints.size(),
                new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(migrationPoints));
        persistenceService.saveMigrationJob(migrationJob);
        migrationJob.setMigrationPoints(new MigrationPoints(
                migrationPoints.migrationPoints.stream().map(
                        mp -> new MigrationPoint(
                                mp.filePath(),
                                mp.description(),
                                mp.recipeId()
                        )
                ).toList(), migrationPoints.overview));
        return migrationJob.getMigrationPoints();
    }

    @AchievesGoal(description = "Modernize codebase")
    @Action
    public MigrationsReport modernize(
            MigrationJob migrationJob,
            MigrationPoints migrationPoints,
            OperationContext context
    ) {
        var softwareProject = migrationJob.softwareProject();
        var migrationReports = new LinkedList<MigrationReport>();
        for (var recipe : migrationJob.getCookbook().getRecipes()) {
            var recipeHits = migrationPoints.getMigrationPoints()
                    .stream()
                    .filter(mp -> Objects.equals(mp.getRecipeId(), recipe.getRecipeId())).toList();

            if (recipeHits.isEmpty()) {
                logger.info("No migration points found for recipe: {} - {}, skipping",
                        recipe.getRecipeId(), recipe.getDescription());
                continue;
            }
            logger.info("Processing migration recipe: {} - {}", recipe.getRecipeId(), recipe.getDescription());

            var originalBranch = softwareProject.currentBranch();
            var branchName = context.getAgentProcess().getId() + "_" + recipe.getRecipeId().toLowerCase();
            var success = softwareProject.createAndCheckoutBranch(branchName);
            logger.info("Migration recipe branch {} created from branch {} - {}", branchName, originalBranch, success);
            var dtos = context.parallelMap(
                    recipeHits,
                    1,
                    mp -> tryToFixIndividualMigrationPoint(
                            migrationJob,
                            mp, context.ai())
            );
            migrationReports.addAll(dtos.stream().map(dto ->
                    new MigrationReport(
                            dto.success(),
                            dto.notes(),
                            dto.branch()
                    )).toList());
            // Go back to the original branch
            logger.info("Switching back from recipe branch {} to original branch {}", branchName, originalBranch);
            softwareProject.checkoutBranch(originalBranch);
        }
        var migrationsReport = new MigrationsReport(migrationReports);
        migrationJob.setMigrationsReport(migrationsReport);
        persistenceService.saveMigrationJob(migrationJob);
        return migrationsReport;
    }

    /**
     * Try to fix an individual migration point
     * Commit if successful, otherwise revert
     */
    private MigrationReportDto tryToFixIndividualMigrationPoint(
            MigrationJob migrationJob,
            MigrationPoint migrationPoint,
            Ai ai) {
        var softwareProject = migrationJob.softwareProject();
        var migrationReport = ai
                .withLlm(config.fixer())
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("fix_migration_point")
                .createObject(
                        MigrationReportDto.class,
                        Map.of(
                                "migrationPoint", migrationPoint
                        ),
                        "fixMigrationPoint"
                );
        if (migrationReport.success()) {
            var message = "Fix: " + migrationPoint.getDescription();
            softwareProject.commit(message, false);
            logger.info("Committing branch {} - {} as migration was successful",
                    softwareProject.currentBranch(), message);
            migrationReport = migrationReport.withBranch(softwareProject.currentBranch());
        } else {
            logger.warn("Reverting branch {} as migration was not successful", softwareProject.currentBranch());
            softwareProject.revert();
        }
        return migrationReport;
    }
}
