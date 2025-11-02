package com.prerana.logs.processor.repository;

import com.prerana.logs.processor.model.StagedLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StagedLogRepository extends JpaRepository<StagedLog, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM StagedLog s")
    void truncateAll();

}
