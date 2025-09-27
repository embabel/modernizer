package com.embabel.modernizer.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.domain.library.code.SoftwareProject;

import java.util.List;

@Agent(description = "Code modernization agent")
public class ModernizerAgent {

    private SoftwareProject softwareProject = new SoftwareProject(
            "/Users/rjohnson/dev/embabel.com/migrater"
    );

    @Action
    public Domain.MigrationPoints migrationPoints(UserInput userInput,
                                                  Ai ai) {
        var text = ai
                .withDefaultLlm()
                .withReferences(softwareProject)
                .generateText("""
                        Answer this question about the given codebase:
                        %s
                        """.formatted(userInput.getContent()));
        System.out.println("******\n" + text + "\n******");
        return new Domain.MigrationPoints(List.of());
    }

    @AchievesGoal(description = "Modernize codebase")
    @Action
    public Domain.MigrationsReport modernize(
            Domain.MigrationPoints migrationPoints
    ) {
        return new Domain.MigrationsReport(List.of());
    }
}
