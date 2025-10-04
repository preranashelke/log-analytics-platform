package com.prerana.logs.ingestion.controller;

import com.prerana.logs.common.dto.LogEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ingestion")
public class LogIngestionController {

    private final KafkaTemplate<String, LogEvent> kafka;

    public LogIngestionController(KafkaTemplate<String, LogEvent> kafka) {
        this.kafka = kafka;
    }

    @Value("${log.topic.name}")
    private String logTopic;

    @PostMapping("/logs")
    public ResponseEntity<Void> ingest(@RequestBody LogEvent event) {

        System.out.println("Received event: " + event);
        kafka.send(logTopic, event.getSource(), event);
        return ResponseEntity.accepted().build();
    }
}
