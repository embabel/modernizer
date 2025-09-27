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

import java.util.List;
import java.util.Map;

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
            "/Users/rjohnson/dev/qct-reference-app1/spring-mysql-app"
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
        System.out.println("******\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(migrationPoints) + "\n******");
        return migrationPoints;
    }

    @AchievesGoal(description = "Modernize codebase")
    @Action
    public Domain.MigrationsReport modernize(
            Domain.MigrationPoints migrationPoints,
            OperationContext context
    ) {
        var migrations = context.parallelMap(
                migrationPoints.migrationPoints(),
                1,
                mp -> tryToFix(mp, context.ai())
        );
        return new Domain.MigrationsReport(migrations);
    }

    private Domain.MigrationReport tryToFix(Domain.MigrationPoint migrationPoint, Ai ai) {
        return ai.withLlmByRole("best")
                .withReferences(softwareProject)
                .withToolObject(new BashTools(softwareProject.getRoot()))
                .withTemplate("fix_migration_point")
                .createObject(
                        Domain.MigrationReport.class,
                        Map.of(
                                "migrationPoint", migrationPoint
                        )
                );
    }
}
