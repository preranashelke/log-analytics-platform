package com.prerana.logs.processor.strategy;

import com.prerana.logs.processor.model.StagedLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory to pick the best-matching strategy for a staged log.
 */
@Slf4j
@Component
public class LogProcessorFactory {

    private final List<LogProcessorStrategy> strategies;
    private final LogProcessorStrategy defaultStrategy;

    public LogProcessorFactory(List<LogProcessorStrategy> strategies, DefaultLogProcessor defaultStrategy) {
        this.strategies = strategies;
        this.defaultStrategy = defaultStrategy;
    }

    public LogProcessorStrategy findFor(StagedLog staged) {
        if (staged == null) return defaultStrategy;
        for (LogProcessorStrategy s : strategies) {

            if (s instanceof DefaultLogProcessor) continue;
            try {
                if (s.supports(staged)) return s;
            } catch (Exception ex) {
                log.warn("strategy {} threw", s.getClass(), ex);
            }
        }
        return defaultStrategy;
    }
}
