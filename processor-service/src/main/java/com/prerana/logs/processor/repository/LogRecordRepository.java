package com.prerana.logs.processor.repository;

import com.prerana.logs.processor.model.LogRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRecordRepository extends JpaRepository<LogRecord, Long> {
}
