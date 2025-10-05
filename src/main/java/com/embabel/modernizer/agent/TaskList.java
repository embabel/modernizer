package com.embabel.modernizer.agent;

import java.util.List;

/**
 * List of focus areas
 *
 * @param classifications
 */
public record TaskList(
        List<Classification> classifications
) {

    public TaskList(Classification... classifications) {
        this(List.of(classifications));
    }

    public static TaskList MODERNIZE_JAVA = new TaskList(
            new Classification("Legacy", "Code that is outdated and may not follow current best practices."),
            new Classification("Deprecated", "Code that uses deprecated libraries or frameworks that are no longer supported."),
            new Classification("Persistence", "Code related to persistence usage"),
            new Classification("Security", "Code related to security")
    );
}
