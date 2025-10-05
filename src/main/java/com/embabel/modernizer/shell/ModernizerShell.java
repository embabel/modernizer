package com.embabel.modernizer.shell;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.modernizer.agent.Domain;
import com.embabel.modernizer.agent.MigrationCookbook;
import com.embabel.modernizer.agent.MigrationRecipe;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
record ModernizerShell(AgentPlatform agentPlatform) {

    @ShellMethod("Modernize a codebase on the local machine")
    String modernize(
            @ShellOption(defaultValue = "/Users/rjohnson/dev/qct-reference-app1") String projectPath
    ) {
        var migrationsReport = AgentInvocation
                .builder(agentPlatform)
                .options(ProcessOptions.builder()
                        .verbosity(v -> v.showPrompts(true))
                        .listener(new ShowCostListener(30))
                        .build())
                .build(Domain.MigrationsReport.class)
                .invoke(
                        new Domain.MigrationJob(
                                projectPath,
                                """
                                        Don't suggest anything risky or address problems not related to older code
                                        LOOK ONLY UNDER the spring-mysql-app directory
                                        """,
                                MigrationCookbook.MODERNIZE_JAVA));
        return migrationsReport + "";
    }

    @ShellMethod("fix logging")
    String fixLogging(
            @ShellOption(defaultValue = "/Users/rjohnson/dev/embabel.com/embabel-agent") String projectPath
    ) {
        var customMigration = new Domain.MigrationJob(
                projectPath,
                """
                        Look only for logging classification
                        """,
                new MigrationCookbook(
                        new MigrationRecipe("Logging",
                                "Logger.log statements should use {} placeholders not literals")));
        var migrationsReport = AgentInvocation.builder(agentPlatform)
                .options(ProcessOptions.builder().verbosity(v -> v.showPrompts(true)).build())
                .build(Domain.MigrationsReport.class)
                .invoke(customMigration);
        return migrationsReport + "";
    }
}

