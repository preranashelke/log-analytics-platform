package com.prerana.logs.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEvent {
    private String eventId;
    private String source;
    private String level;
    private String message;
    private Map<String, String> attributes;

    private Instant timestamp;

    public static LogEvent create(String source, String level,
                                  String message, Map<String,String> attributes) {
        return LogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .source(source) .level(level)
                .message(message)
                .attributes(attributes)
                .timestamp(Instant.now()) .build();
    }
}
