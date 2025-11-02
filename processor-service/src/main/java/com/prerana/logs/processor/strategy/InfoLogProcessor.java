package com.prerana.logs.processor.strategy;


import com.prerana.logs.processor.model.LogRecord;
import com.prerana.logs.processor.model.StagedLog;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Processor for INFO-level logs.
 */
@Component
@Log4j2
public class InfoLogProcessor implements LogProcessorStrategy {

    @Override
    public boolean supports(StagedLog staged) {
        return staged != null && "INFO".equalsIgnoreCase(staged.getLevel());
    }

    @Override
    public LogRecord process(StagedLog staged) {

        log.info("Using InfoLogProcessor for {}", staged.getEventId());

        LogRecord r = new LogRecord();
        r.setEventId(staged.getEventId());
        r.setSource(staged.getSource());
        r.setLevel(staged.getLevel());
        r.setMessage(staged.getMessage());
        r.setAttributes(staged.getAttributes());
        r.setTimestamp(staged.getTimestamp());
        r.setIngestedAt(LocalDateTime.now());

        return r;
    }
}
