package com.prerana.logs.processor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prerana.logs.common.dto.LogEvent;
import com.prerana.logs.processor.model.StagedLog;
import com.prerana.logs.processor.repository.StagedLogRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@Log4j2
public class LogBatchConsumer {

    private final StagedLogRepository stagedRepo;
    private final KafkaTemplate<String, LogEvent> kafkaTemplate;

    public LogBatchConsumer(StagedLogRepository stagedRepo,
                            KafkaTemplate<String, LogEvent> kafkaTemplate) {
        this.stagedRepo = stagedRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${log.dlq.topic.name}")
    private String dlqTopic;

    @KafkaListener(topics = "${log.topic.name}", containerFactory = "batchFactory")
    public void onMessage(List<LogEvent> events, Acknowledgment ack, Consumer<?, ?> consumer) {
        try {

            List<StagedLog> rows = events.stream().map(e -> {
                StagedLog s = new StagedLog();
                s.setEventId(e.getEventId());
                s.setSource(e.getSource());
                s.setLevel(e.getLevel());
                s.setMessage(e.getMessage());

                try {
                    s.setAttributes(new ObjectMapper().writeValueAsString(e.getAttributes()));
                } catch (Exception ex) {
                    s.setAttributes("{}");
                }
                s.setTimestamp(LocalDateTime.ofInstant(e.getTimestamp(), ZoneId.systemDefault()));
                s.setIngestedAt(LocalDateTime.now());
                return s;
            }).toList();

            //saving logs in db
            stagedRepo.saveAll(rows);
            ack.acknowledge();

        } catch (Exception ex) {
            log.info("sending unknown logs to dlq");
            events.forEach(e -> kafkaTemplate.send(dlqTopic, e.getSource(), e));
            ack.acknowledge();
        }
    }
}
