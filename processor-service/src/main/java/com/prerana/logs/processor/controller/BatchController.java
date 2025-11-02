package com.prerana.logs.processor.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/batch")
@Log4j2
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job processStagedLogsJob;

    @Autowired
    public BatchController(JobLauncher jobLauncher,
                           @Qualifier("processStagedLogsJob") Job processStagedLogsJob) {
        this.jobLauncher = jobLauncher;
        this.processStagedLogsJob = processStagedLogsJob;
    }

    @PostMapping("/run-batch")
    public ResponseEntity<String> runBatch() throws Exception {
        try {
            JobExecution exec = jobLauncher.run(processStagedLogsJob,
                    new org.springframework.batch.core.JobParametersBuilder()
                            .addLong("timestamp", System.currentTimeMillis())
                            .toJobParameters());

            log.info("Batch job triggered successfully (Job ID: {}, Status: {})",
                    exec.getId(), exec.getStatus());
            return ResponseEntity.ok("Batch job started. Job ID: " + exec.getId());
        } catch (Exception e) {
            log.error("Failed to run batch job", e);
            return ResponseEntity.status(500).body("Batch start failed: " + e.getMessage());
        }
    }
}
