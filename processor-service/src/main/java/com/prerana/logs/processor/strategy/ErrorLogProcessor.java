package com.prerana.logs.processor.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prerana.logs.processor.model.LogRecord;
import com.prerana.logs.processor.model.StagedLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processor for ERROR-level logs.
 */
@Slf4j
@Component
public class ErrorLogProcessor implements LogProcessorStrategy {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(StagedLog staged) {
        return staged != null && "ERROR".equalsIgnoreCase(staged.getLevel());
    }

    @Override
    public LogRecord process(StagedLog staged) throws Exception {
        log.info("Using ErrorLogProcessor for {}", staged.getEventId());
        LogRecord r = new LogRecord();
        r.setEventId(staged.getEventId());
        r.setSource(staged.getSource());
        r.setLevel(staged.getLevel());
        r.setMessage(staged.getMessage());
        r.setTimestamp(staged.getTimestamp());
        r.setIngestedAt(LocalDateTime.now());

        Map<String, Object> attrs = new LinkedHashMap<>();
        if (staged.getAttributes() != null && !staged.getAttributes().trim().isEmpty()) {
            try {
                attrs = mapper.readValue(staged.getAttributes(), Map.class);
            } catch (Exception e) {
                log.error("ignoring parse errors");
            }
        }

        log.info("extracting the first line as errorType if message contains Exception or stacktrace lines.");
        String msg = staged.getMessage() == null ? "" : staged.getMessage();
        String errorType = null;
        if (msg.contains("Exception") || msg.contains("Error")) {
            String firstLine = msg.split("\\r?\\n")[0];
            errorType = firstLine.length() > 200 ? firstLine.substring(0, 200) : firstLine;
            attrs.put("errorType", errorType);
        }


        log.info("extracting orderId if present in attributes or message.");
        if (!attrs.containsKey("orderId")) {
            String lower = msg.toLowerCase();
            int idx = lower.indexOf("orderid=");
            if (idx > -1) {
                String token = msg.substring(idx + "orderid=".length()).split("[\\s,;]")[0];
                attrs.put("orderId", token);
            }
        }

        log.info("Converting attributes map back to JSON string for storage");
        r.setAttributes(mapper.writeValueAsString(attrs));

        return r;
    }
}
