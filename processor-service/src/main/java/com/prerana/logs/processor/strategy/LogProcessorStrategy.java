package com.prerana.logs.processor.strategy;

import com.prerana.logs.processor.model.LogRecord;
import com.prerana.logs.processor.model.StagedLog;

/**
 * Strategy interface for processing staged logs into final LogRecord entities.
 */
public interface LogProcessorStrategy {

    boolean supports(StagedLog staged);

    LogRecord process(StagedLog staged) throws Exception;
}
