package com.prerana.logs.processor.batch;

import com.prerana.logs.common.dto.LogEvent;
import com.prerana.logs.processor.model.StagedLog;
import com.prerana.logs.processor.repository.StagedLogRepository;
import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Listens for skipped items during processing and publishes to DLQ topic.
 */
@Component
public class LogSkipListener implements SkipListener<StagedLog, Object> {

    private final KafkaTemplate<String, LogEvent> kafkaTemplate;

    public LogSkipListener(KafkaTemplate<String, LogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${log.dlq.topic.name}")
    private String dlqTopic;

    @Override
    public void onSkipInRead(Throwable t) {
    }


    @Override
    public void onSkipInProcess(StagedLog item, Throwable t) {
        try {
            LogEvent event = new LogEvent();
            event.setSource(item.getSource());
            event.setLevel(item.getLevel());
            event.setMessage(item.getMessage());
            kafkaTemplate.send(dlqTopic, event.getSource(), event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
