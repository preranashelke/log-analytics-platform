package com.prerana.logs.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class LogRequest {
    @NotBlank
    private String source;

    @NotBlank
    private String level;

    @NotBlank
    private String message;

    @NotNull
    private Instant timestamp = Instant.now();

    private Map<String,String> attributes;

    public String getSource() { return source; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, String> getAttributes() { return attributes; }
}
