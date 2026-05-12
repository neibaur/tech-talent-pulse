package com.techtalentpulse.orchestration.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.techtalentpulse.orchestration.application.OperationalHistoryService;
import com.techtalentpulse.orchestration.application.TechnologySignalOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminOrchestrationController.class)
@TestPropertySource(properties = "tech-talent-pulse.admin.orchestration.enabled=false")
class AdminOrchestrationControllerDisabledTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TechnologySignalOrchestrationService orchestrationService;

  @MockitoBean private OperationalHistoryService operationalHistoryService;

  @Test
  void adminOrchestrationEndpointIsUnavailableWhenDisabled() throws Exception {
    mockMvc.perform(post("/api/admin/orchestration/pipeline")).andExpect(status().isNotFound());
    mockMvc.perform(get("/api/admin/orchestration/runs")).andExpect(status().isNotFound());
  }
}
