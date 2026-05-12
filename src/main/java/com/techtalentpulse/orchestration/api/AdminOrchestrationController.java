package com.techtalentpulse.orchestration.api;

import com.techtalentpulse.orchestration.application.OrchestrationResult;
import com.techtalentpulse.orchestration.application.TechnologySignalOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orchestration")
@ConditionalOnProperty(name = "tech-talent-pulse.admin.orchestration.enabled", havingValue = "true")
public class AdminOrchestrationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminOrchestrationController.class);

  private final TechnologySignalOrchestrationService orchestrationService;

  public AdminOrchestrationController(TechnologySignalOrchestrationService orchestrationService) {
    this.orchestrationService = orchestrationService;
  }

  @PostMapping("/ingestion")
  public ResponseEntity<OrchestrationResponse> runIngestion() {
    LOGGER.info("admin_orchestration_trigger_requested trigger=ingestion");
    OrchestrationResult result = orchestrationService.runIngestion();
    logCompleted("ingestion", result);
    return ResponseEntity.ok(OrchestrationResponse.from(result));
  }

  @PostMapping("/transformation")
  public ResponseEntity<OrchestrationResponse> runTransformation() {
    LOGGER.info("admin_orchestration_trigger_requested trigger=transformation");
    OrchestrationResult result = orchestrationService.runTransformation();
    logCompleted("transformation", result);
    return ResponseEntity.ok(OrchestrationResponse.from(result));
  }

  @PostMapping("/pipeline")
  public ResponseEntity<OrchestrationResponse> runPipeline() {
    LOGGER.info("admin_orchestration_trigger_requested trigger=pipeline");
    OrchestrationResult result = orchestrationService.runIngestionAndTransformation();
    logCompleted("pipeline", result);
    return ResponseEntity.ok(OrchestrationResponse.from(result));
  }

  private void logCompleted(String trigger, OrchestrationResult result) {
    LOGGER.info(
        "admin_orchestration_trigger_completed trigger={} status={} fetched={} persisted={} duplicates={} snapshots={} message={}",
        trigger,
        result.status(),
        result.fetchedCount(),
        result.persistedCount(),
        result.duplicateCount(),
        result.transformedSnapshotCount(),
        result.message());
  }
}
