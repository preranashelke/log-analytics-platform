package com.prerana.logs.processor.batch;

import com.prerana.logs.processor.model.LogRecord;
import com.prerana.logs.processor.model.StagedLog;
import com.prerana.logs.processor.strategy.LogProcessorFactory;
import com.prerana.logs.processor.strategy.LogProcessorStrategy;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private LogProcessorFactory strategyFactory;

    @Autowired
    private LogSkipListener logSkipListener;

    @Autowired
    private CleanupTasklet cleanupTasklet;


    @Bean
    public Job processStagedLogsJob() {
        return new JobBuilder("processStagedLogsJob", jobRepository)
                .start(processStagedLogsStep())
                .next(cleanupStep())
                .build();
    }

    @Bean
    public Step cleanupStep() {
        return new StepBuilder("cleanupStep", jobRepository)
                .tasklet(cleanupTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step processStagedLogsStep() {
        return new StepBuilder("processStagedLogsStep", jobRepository)
                .<StagedLog, LogRecord>chunk(300, transactionManager)
                .reader(stagedLogReader())
                .processor(itemProcessor())
                .writer(jpaItemWriter())
                .faultTolerant()
                .skipLimit(1000)
                .skip(Exception.class)
                .listener(logSkipListener)
                .build();
    }

    @Bean
    public ItemReader<StagedLog> stagedLogReader() {
        JpaPagingItemReader<StagedLog> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT s FROM StagedLog s ORDER BY s.id ASC");
        reader.setPageSize(300);
        reader.setSaveState(false);
        return reader;
    }


    @Bean
    public ItemProcessor<StagedLog, LogRecord> itemProcessor() {
        return staged -> {
            LogProcessorStrategy strat = strategyFactory.findFor(staged);
            LogRecord record = strat.process(staged);
            return record;
        };
    }

    @Bean
    public ItemWriter<LogRecord> jpaItemWriter() {
        JpaItemWriter<LogRecord> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
