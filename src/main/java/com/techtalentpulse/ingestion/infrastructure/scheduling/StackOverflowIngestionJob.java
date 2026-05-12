package com.techtalentpulse.ingestion.infrastructure.scheduling;

import com.techtalentpulse.ingestion.application.StackOverflowIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "tech-talent-pulse.stack-exchange",
    name = "scheduler-enabled",
    havingValue = "true")
public class StackOverflowIngestionJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(StackOverflowIngestionJob.class);

  private final StackOverflowIngestionService ingestionService;

  public StackOverflowIngestionJob(StackOverflowIngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @Scheduled(fixedDelayString = "${tech-talent-pulse.stack-exchange.scheduler-fixed-delay:PT6H}")
  public void ingestStackOverflowQuestions() {
    LOGGER.info("stack_overflow_ingestion_job_started");
    ingestionService.ingestConfiguredTags();
  }
}
