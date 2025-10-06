package com.embabel.modernizer.shell;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.modernizer.entity.MigrationCookbook;
import com.embabel.modernizer.entity.MigrationJob;
import com.embabel.modernizer.entity.MigrationRecipe;
import com.embabel.modernizer.entity.MigrationsReport;
import com.embabel.modernizer.service.PersistenceService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;

import java.util.List;
import java.util.UUID;

@ShellComponent
record ModernizerShell(
        AgentPlatform agentPlatform,
        PersistenceService migrationService) {

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
                .build(MigrationsReport.class)
                .invoke(
                        new MigrationJob(
                                UUID.randomUUID().toString(),
                                projectPath,
                                """
                                        Don't suggest anything risky or address problems not related to older code
                                        
                                        LOOK ONLY UNDER the spring-mysql-app directory
                                        and only in the com.example.service package
                                        STOP WHEN YOUVE FOUND 2 SUGGESTIONS
                                        """,
                                MigrationCookbook.MODERNIZE_JAVA));
        return migrationsReport + "";
    }

    @ShellMethod("fix logging")
    String fixLogging(
            @ShellOption(defaultValue = "/Users/rjohnson/dev/embabel.com/embabel-agent") String projectPath
    ) {
        var customMigration = new MigrationJob(
                UUID.randomUUID().toString(),
                projectPath,
                """
                        Look only for logging classification
                        """,
                new MigrationCookbook(
                        "LOGGING_FIX",
                        new MigrationRecipe("Logging",
                                "Logger.log statements should use {} placeholders not literals")));
        var migrationsReport = AgentInvocation.builder(agentPlatform)
                .options(ProcessOptions.builder().verbosity(v -> v.showPrompts(true)).build())
                .build(List.class)
                .invoke(customMigration);
        return migrationsReport + "";
    }

    @ShellMethod("List all migration jobs")
    String listJobs() {
        var jobs = migrationService.findAllMigrationJobs();

        if (jobs.isEmpty()) {
            return "No migration jobs found";
        }

        String[][] data = new String[jobs.size() + 1][];
        data[0] = new String[]{"ID", "Job ID", "Project Root", "Cookbook", "Created At"};

        for (int i = 0; i < jobs.size(); i++) {
            var job = jobs.get(i);
            data[i + 1] = new String[]{
                    String.valueOf(job.getId()),
                    job.getJobId(),
                    job.getProjectRoot(),
                    job.getCookbook() != null ? job.getCookbook().getName() : "N/A",
                    job.getCreatedAt().toString()
            };
        }

        Table table = new TableBuilder(new ArrayTableModel(data))
                .addHeaderAndVerticalsBorders(BorderStyle.fancy_light)
                .build();

        return table.render(120);
    }

    @ShellMethod("List all migration points")
    String listPoints(@ShellOption(help = "Migration job ID") Long jobId) {
        var job = migrationService.findMigrationJobByJobId(String.valueOf(jobId));
        if (job.isEmpty()) {
            return "No migration job found with ID: " + jobId;
        }

        var migrationPoints = job.get().getMigrationPoints();
        if (migrationPoints == null || migrationPoints.getMigrationPoints().isEmpty()) {
            return "No migration points found for job ID: " + jobId;
        }

        var points = migrationPoints.getMigrationPoints();

        String[][] data = new String[points.size() + 1][];
        data[0] = new String[]{"ID", "File Path", "Recipe ID", "Description"};

        for (int i = 0; i < points.size(); i++) {
            var point = points.get(i);
            data[i + 1] = new String[]{
                    String.valueOf(point.getId()),
                    point.getFilePath(),
                    point.getRecipeId(),
                    truncate(point.getDescription(), 50)
            };
        }

        Table table = new TableBuilder(new ArrayTableModel(data))
                .addHeaderAndVerticalsBorders(BorderStyle.fancy_light)
                .build();

        return table.render(120);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}

