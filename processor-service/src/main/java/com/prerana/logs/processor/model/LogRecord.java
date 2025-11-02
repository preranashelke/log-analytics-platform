package com.prerana.logs.processor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_record", uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;
    private String source;
    private String level;
    @Column(columnDefinition = "TEXT")
    private String message;
    @Column(columnDefinition = "json")
    private String attributes;
    private LocalDateTime timestamp;
    private LocalDateTime ingestedAt;
}
