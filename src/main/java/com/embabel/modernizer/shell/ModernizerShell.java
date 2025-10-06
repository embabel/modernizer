package com.embabel.modernizer.shell;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.modernizer.agent.Domain;
import com.embabel.modernizer.agent.MigrationCookbook;
import com.embabel.modernizer.agent.MigrationRecipe;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;

@ShellComponent
record ModernizerShell(
        AgentPlatform agentPlatform,
        ObjectMapper objectMapper) {

    @ShellMethod("Modernize a codebase on the local machine")
    String modernize(
            @ShellOption(defaultValue = "/Users/rjohnson/dev/qct-reference-app1") String projectPath
    ) throws IOException {
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
                                        IMPORTANT: RETURN AFTER YOU'VE FOUND 2 problems
                                        """,
                                MigrationCookbook.MODERNIZE_JAVA));

        var branches = migrationsReport.branchesCreated().stream()
                .map(b -> "\t" + b)
                .collect(java.util.stream.Collectors.joining("\n"));

        return "Branches created (" + migrationsReport.branchesCreated().size() + "):\n" +
                branches + "\n\n" +
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(migrationsReport);
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

