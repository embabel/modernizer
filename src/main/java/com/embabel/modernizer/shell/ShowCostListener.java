package com.embabel.modernizer.shell;

import com.embabel.agent.event.AgentProcessEvent;
import com.embabel.agent.event.AgenticEventListener;
import com.embabel.agent.event.ToolCallResponseEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Show cost after the given number of seconds
 */
public class ShowCostListener implements AgenticEventListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicReference<Instant> lastLog = new AtomicReference<>(Instant.now());

    private final int logIntervalSeconds;

    public ShowCostListener(int logIntervalSeconds) {
        this.logIntervalSeconds = logIntervalSeconds;
    }

    @Override
    public void onProcessEvent(@NotNull AgentProcessEvent event) {
        if (event instanceof ToolCallResponseEvent tcre && tcre.getAgentProcess().usage().getPromptTokens() > 0) {
            Instant now = Instant.now();
            Instant last = lastLog.get();
            if (Duration.between(last, now).toSeconds() > logIntervalSeconds) {
                if (lastLog.compareAndSet(last, now)) {
                    logger.info("Usage: {}, cost: $%.2f".formatted(tcre.getAgentProcess().cost()),
                            tcre.getAgentProcess().usage());
                }
            }
        }
    }
}
