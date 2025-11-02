package com.prerana.logs.ingestion.controller;

import com.prerana.logs.common.dto.LogEvent;
import com.prerana.logs.ingestion.dto.LogRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ingestion")
@Validated
public class LogIngestionController {

    private final KafkaTemplate<String, LogEvent> kafka;

    public LogIngestionController(KafkaTemplate<String, LogEvent> kafka) {
        this.kafka = kafka;
    }

    @Value("${log.topic.name}")
    private String logTopic;

    @PostMapping("/logs")
    public ResponseEntity<Void> ingest(@Valid @RequestBody LogRequest req) {

        LogEvent event = LogEvent.builder()
                .eventId(null)
                .source(req.getSource())
                .level(req.getLevel())
                .message(req.getMessage())
                .timestamp(req.getTimestamp())
                .build();

        System.out.println("Received event: " + event);
        kafka.send(logTopic, event.getSource(), event);
        return ResponseEntity.accepted().build();
    }
}
