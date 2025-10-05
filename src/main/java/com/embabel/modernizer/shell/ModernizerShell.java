package com.embabel.modernizer.shell;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.modernizer.agent.Classification;
import com.embabel.modernizer.agent.Domain;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
record ModernizerShell(AgentPlatform agentPlatform) {


    @ShellMethod("Modernize")
    String modernize(
            @ShellOption(defaultValue = "/Users/rjohnson/dev/qct-reference-app1") String projectPath
    ) {

        var migrationsReport = AgentInvocation
                .create(agentPlatform, Domain.MigrationsReport.class)
                .invoke(new Domain.MigrationTask(
                        projectPath,
                        """
                                Don't suggest anything risky or address problems not related to older code
                                LOOK ONLY UNDER the spring-mysql-app directory
                                """));
        return migrationsReport + "";
    }

    @ShellMethod("fix logs")
    String fixlog(
            @ShellOption(defaultValue = "/Users/rjohnson/dev/embabel.com/embabel-agent") String projectPath
    ) {
        var customMigration = new Domain.MigrationTask(
                projectPath,
                """
                        Look only for logging classification
                        """,
                List.of(new Classification("Logging",
                        "Logger.log statements should use {} placeholders not literals")));
        var migrationsReport = AgentInvocation.builder(agentPlatform)
                .options(ProcessOptions.builder().verbosity(v -> v.showPrompts(true)).build())
                .build(Domain.MigrationsReport.class)
                .invoke(customMigration);
        return migrationsReport + "";
    }
}

