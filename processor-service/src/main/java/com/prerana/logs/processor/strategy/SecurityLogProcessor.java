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
 * Processor for security-related logs.
 */
@Component
@Slf4j
public class SecurityLogProcessor implements LogProcessorStrategy {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(StagedLog staged) {
        if (staged == null) return false;
        if ("SECURITY".equalsIgnoreCase(staged.getLevel())) return true;
        String s = staged.getSource() == null ? "" : staged.getSource().toLowerCase();
        if (s.contains("auth") || s.contains("security")) return true;
        return false;
    }

    @Override
    public LogRecord process(StagedLog staged) throws Exception {

        log.info("Using SecurityLogProcessor for {}", staged.getEventId());

        LogRecord r = new LogRecord();
        r.setEventId(staged.getEventId());
        r.setSource(staged.getSource());
        r.setLevel(staged.getLevel());
        r.setMessage(staged.getMessage());
        r.setTimestamp(staged.getTimestamp());
        r.setIngestedAt(LocalDateTime.now());

        Map<String, Object> attrs = new LinkedHashMap<>();
        if (staged.getAttributes() != null && !staged.getAttributes().isBlank()) {
            try {
                attrs = mapper.readValue(staged.getAttributes(), Map.class);
            } catch (Exception ex) {
                log.error("ignoring parse errors");
            }
        }

        log.info("Extracting common security fields if present");
        if (attrs.containsKey("user")) {
            attrs.put("user", attrs.get("user"));
        } else {
            String msg = staged.getMessage() == null ? "" : staged.getMessage();
            if (msg.contains("user=")) {
                String token = msg.substring(msg.indexOf("user=") + 5).split("[\\s,;]")[0];
                attrs.put("user", token);
            }
        }

        if (!attrs.containsKey("ip") && staged.getMessage() != null) {
            String msg = staged.getMessage();
            String ip = extractFirstIp(msg);
            if (ip != null) attrs.put("ip", ip);
        }

        attrs.put("securityFlag", true);

        r.setAttributes(mapper.writeValueAsString(attrs));
        return r;
    }

    private String extractFirstIp(String msg) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)"
        ).matcher(msg);
        if (m.find()) return m.group();
        return null;
    }
}
