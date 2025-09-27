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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Agent(description = "Code modernization agent")
public class ModernizerAgent {

    private final Logger logger = LoggerFactory.getLogger(ModernizerAgent.class);

    private int modificationCount = 0;

    private final List<Classification> classifications = List.of(
            new Classification("Legacy", "Code that is outdated and may not follow current best practices."),
            new Classification("Deprecated", "Code that uses deprecated libraries or frameworks that are no longer supported."),
            new Classification("Persistence", "Code related to persistence usage"),
            new Classification("Security", "Code related to security")
    );


    private SoftwareProject softwareProject = new SoftwareProject(
            "/Users/rjohnson/dev/qct-reference-app1/spring-mysql-app"
    );

    private final Repository repository;
    private final Git git;

    public ModernizerAgent() {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            this.repository = builder
                    .setGitDir(Path.of(
                            "/Users/rjohnson/dev/qct-reference-app1",
                            // softwareProject.getRoot(),
                            ".git").toFile())
                    .build();
            this.git = new Git(repository);

            // Note: In many JGit use cases, Git instances should be closed using try-with-resources.
            // However, in this case, the Git instance is a class field that needs to remain open
            // for the lifetime of the agent, so we intentionally don't close it here.
        } catch (IOException e) {
            logger.error("Failed to initialize Git repository", e);
            throw new RuntimeException("Could not initialize Git repository", e);
        }
    }

    /**
     * Creates a new branch from the current HEAD
     *
     * @param branchName the name of the new branch
     * @return true if branch was created successfully
     */
    // TODO git functionality could be on SoftwareProject
    public boolean createBranch(String branchName) {
        try {
            // Check if repository has any commits
            if (!hasCommits()) {
                logger.warn("Repository has no commits yet, creating initial commit first");
                if (!createInitialCommit()) {
                    logger.error("Failed to create initial commit");
                    return false;
                }
            }

            git.branchCreate()
                    .setName(branchName)
                    .call();

            logger.info("Created branch: {}", branchName);
            return true;
        } catch (Exception e) {
            logger.error("Failed to create branch: {}", branchName, e);
            return false;
        }
    }

    private boolean hasCommits() {
        try {
            var headRef = repository.exactRef("HEAD");
            return headRef != null && headRef.getObjectId() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean createInitialCommit() {
        try {
            // Add all files and create initial commit
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
            logger.info("Created initial commit");
            return true;
        } catch (Exception e) {
            logger.error("Failed to create initial commit", e);
            return false;
        }
    }

    public void checkoutBranch(String branchName) {
        try {
            git.checkout()
                    .setName(branchName)
                    .call();
            logger.info("Checked out branch: {}", branchName);
        } catch (Exception e) {
            logger.error("Failed to checkout branch: {}", branchName, e);
        }
    }

    public String currentBranch() {
        try {
            var head = repository.exactRef("HEAD");
            if (head != null && head.isSymbolic()) {
                var target = head.getTarget();
                if (target != null) {
                    var branchName = target.getName();
                    if (branchName.startsWith("refs/heads/")) {
                        return branchName.substring("refs/heads/".length());
                    } else {
                        return branchName;
                    }
                }
            }
            return "unknown";
        } catch (Exception e) {
            logger.error("Failed to get current branch", e);
            return "error";
        }
    }

    /**
     * Creates and checks out a new branch
     *
     * @param branchName the name of the new branch
     * @return true if branch was created and checked out successfully
     */
    public boolean createAndCheckoutBranch(String branchName) {
        try {
            git.checkout()
                    .setCreateBranch(true)
                    .setName(branchName)
                    .call();
            logger.info("Created and checked out branch: {}", branchName);
            return true;
        } catch (Exception e) {
            logger.error("Failed to create and checkout branch: {}", branchName, e);
            return false;
        }
    }


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
        var migrations = context.parallelMap(
                migrationPoints.migrationPoints(),
                1,
                mp -> tryToFix(mp, context)
        );
        return new Domain.MigrationsReport(migrations);
    }

    private Domain.MigrationReport tryToFix(
            Domain.MigrationPoint migrationPoint,
            OperationContext operationContext) {
        var currentBranch = currentBranch();
        var branchName = operationContext.getAgentProcess().getId() + "-embabel-mod-" + modificationCount++;
        createBranch(branchName);
        var migrationReport = operationContext.ai()
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
            logger.info("Keeping branch {} we created", branchName);
            migrationReport = migrationReport.withBranch(branchName);
        } else {
            try {
                logger.info("Deleting branch {} as migration was not successful", branchName);
                git.branchDelete()
                        .setBranchNames(branchName)
                        .setForce(true)
                        .call();
            } catch (Exception e) {
                logger.error("Failed to delete branch: {}", branchName, e);
            }
        }
        checkoutBranch(currentBranch);

        return migrationReport;
    }
}
