package com.prerana.logs.ingestion.generator;

import com.prerana.logs.common.dto.LogEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyntheticLogGenerator {

    private final KafkaTemplate<String, LogEvent> kafka;
    private int counter = 0;

    public SyntheticLogGenerator(KafkaTemplate<String, LogEvent> kafka) {
        this.kafka = kafka;
    }


    @Scheduled(fixedRateString = "${generator.rate:1800000}")
    public void produceBatch() {
        for (int i=0;i<5;i++) {
            String source = "pos-" + (counter%10);
            LogEvent event = LogEvent.builder()
                    .source(source)
                    .level("INFO")
                    .message("synthetic message " + counter++)
                    .build();
            kafka.send("logs", source, event);
        }
    }
}