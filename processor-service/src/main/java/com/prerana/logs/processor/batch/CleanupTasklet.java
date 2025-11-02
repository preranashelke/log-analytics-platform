package com.prerana.logs.processor.batch;

import com.prerana.logs.processor.repository.StagedLogRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class CleanupTasklet implements Tasklet {

    private final StagedLogRepository stagedLogRepository;

    public CleanupTasklet(StagedLogRepository stagedLogRepository) {
        this.stagedLogRepository = stagedLogRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        stagedLogRepository.truncateAll();
        log.info("Cleaned up staged_log table after batch completion");
        return RepeatStatus.FINISHED;
    }
}
