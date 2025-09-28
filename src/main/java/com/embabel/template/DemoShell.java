package com.embabel.template;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.modernizer.agent.Domain;
import com.embabel.template.agent.WriteAndReviewAgent;
import com.embabel.template.injected.InjectedDemo;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
record DemoShell(InjectedDemo injectedDemo, AgentPlatform agentPlatform) {

    @ShellMethod("Demo")
    String demo() {
        // Illustrate calling an agent programmatically,
        // as most often occurs in real applications.
        var reviewedStory = AgentInvocation
                .create(agentPlatform, WriteAndReviewAgent.ReviewedStory.class)
                .invoke(new UserInput("Tell me a story about caterpillars"));
        return reviewedStory.getContent();
    }

    @ShellMethod("Invent an animal")
    String animal() {
        return injectedDemo.inventAnimal().toString();
    }

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
}
