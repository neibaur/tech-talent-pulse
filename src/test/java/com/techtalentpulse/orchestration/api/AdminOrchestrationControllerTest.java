package com.techtalentpulse.orchestration.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.techtalentpulse.ingestion.domain.IngestionProvider;
import com.techtalentpulse.ingestion.domain.IngestionRunStatus;
import com.techtalentpulse.orchestration.application.IngestionRunHistory;
import com.techtalentpulse.orchestration.application.OperationalHistoryService;
import com.techtalentpulse.orchestration.application.OrchestrationResult;
import com.techtalentpulse.orchestration.application.OrchestrationStatus;
import com.techtalentpulse.orchestration.application.TechnologySignalOrchestrationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminOrchestrationController.class)
@TestPropertySource(properties = "tech-talent-pulse.admin.orchestration.enabled=true")
class AdminOrchestrationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TechnologySignalOrchestrationService orchestrationService;

  @MockitoBean private OperationalHistoryService operationalHistoryService;

  @Test
  void ingestionEndpointReturnsOrchestrationResult() throws Exception {
    when(orchestrationService.runIngestion()).thenReturn(result(2, 1, 1, 0));

    mockMvc
        .perform(post("/api/admin/orchestration/ingestion"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.provider").value("STACK_OVERFLOW"))
        .andExpect(jsonPath("$.startedAt").value("2026-01-05T12:00:00Z"))
        .andExpect(jsonPath("$.completedAt").value("2026-01-05T12:00:30Z"))
        .andExpect(jsonPath("$.fetchedCount").value(2))
        .andExpect(jsonPath("$.persistedCount").value(1))
        .andExpect(jsonPath("$.duplicateCount").value(1))
        .andExpect(jsonPath("$.transformedSnapshotCount").value(0))
        .andExpect(jsonPath("$.message").value("completed"));
  }

  @Test
  void transformationEndpointReturnsOrchestrationResult() throws Exception {
    when(orchestrationService.runTransformation()).thenReturn(result(0, 0, 0, 3));

    mockMvc
        .perform(post("/api/admin/orchestration/transformation"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.transformedSnapshotCount").value(3));
  }

  @Test
  void pipelineEndpointReturnsOrchestrationResult() throws Exception {
    when(orchestrationService.runIngestionAndTransformation()).thenReturn(result(4, 3, 1, 2));

    mockMvc
        .perform(post("/api/admin/orchestration/pipeline"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.fetchedCount").value(4))
        .andExpect(jsonPath("$.persistedCount").value(3))
        .andExpect(jsonPath("$.duplicateCount").value(1))
        .andExpect(jsonPath("$.transformedSnapshotCount").value(2));
  }

  @Test
  void recentRunsEndpointReturnsIngestionRunHistory() throws Exception {
    UUID runId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(operationalHistoryService.effectiveLimit(5)).thenReturn(5);
    when(operationalHistoryService.getRecentIngestionRuns(5))
        .thenReturn(
            List.of(
                new IngestionRunHistory(
                    runId,
                    IngestionProvider.STACK_OVERFLOW,
                    IngestionRunStatus.COMPLETED,
                    Instant.parse("2026-01-05T12:00:00Z"),
                    Instant.parse("2026-01-05T12:00:30Z"),
                    null,
                    25,
                    20,
                    22,
                    2)));

    mockMvc
        .perform(get("/api/admin/orchestration/runs").param("limit", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(runId.toString()))
        .andExpect(jsonPath("$[0].provider").value("STACK_OVERFLOW"))
        .andExpect(jsonPath("$[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$[0].startedAt").value("2026-01-05T12:00:00Z"))
        .andExpect(jsonPath("$[0].completedAt").value("2026-01-05T12:00:30Z"))
        .andExpect(jsonPath("$[0].itemsRequested").value(25))
        .andExpect(jsonPath("$[0].itemsCaptured").value(20))
        .andExpect(jsonPath("$[0].itemsFetched").value(22))
        .andExpect(jsonPath("$[0].itemsDuplicateSkipped").value(2));
  }

  private OrchestrationResult result(
      int fetchedCount, int persistedCount, int duplicateCount, int snapshotCount) {
    return new OrchestrationResult(
        OrchestrationStatus.COMPLETED,
        IngestionProvider.STACK_OVERFLOW,
        Instant.parse("2026-01-05T12:00:00Z"),
        Instant.parse("2026-01-05T12:00:30Z"),
        fetchedCount,
        persistedCount,
        duplicateCount,
        snapshotCount,
        "completed");
  }
}
