package com.techtalentpulse.dashboard.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.techtalentpulse.dashboard.application.DashboardTrendService;
import com.techtalentpulse.dashboard.domain.TopTagTrend;
import com.techtalentpulse.dashboard.domain.TrendSnapshot;
import com.techtalentpulse.dashboard.domain.TrendSummary;
import com.techtalentpulse.ingestion.domain.IngestionProvider;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardTrendController.class)
class DashboardTrendControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DashboardTrendService dashboardTrendService;

  @Test
  void returnsRecentTrendSnapshots() throws Exception {
    when(dashboardTrendService.recentSnapshots(25))
        .thenReturn(
            List.of(
                new TrendSnapshot(
                    "java",
                    IngestionProvider.STACK_OVERFLOW,
                    LocalDate.parse("2026-01-02"),
                    12,
                    4.5,
                    1.75)));

    mockMvc
        .perform(get("/api/trends").param("limit", "25"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].tag").value("java"))
        .andExpect(jsonPath("$[0].provider").value("STACK_OVERFLOW"))
        .andExpect(jsonPath("$[0].snapshotDate").value("2026-01-02"))
        .andExpect(jsonPath("$[0].signalCount").value(12))
        .andExpect(jsonPath("$[0].averageScore").value(4.5))
        .andExpect(jsonPath("$[0].averageAnswerCount").value(1.75));
  }

  @Test
  void returnsTrendHistoryForTag() throws Exception {
    when(dashboardTrendService.trendHistory("docker", null)).thenReturn(List.of());

    mockMvc
        .perform(get("/api/trends/docker"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void returnsTrendSummary() throws Exception {
    when(dashboardTrendService.summary(3))
        .thenReturn(
            new TrendSummary(
                LocalDate.parse("2026-01-03"),
                List.of(new TopTagTrend("kubernetes", 30), new TopTagTrend("java", 20))));

    mockMvc
        .perform(get("/api/trends/summary").param("limit", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mostRecentSnapshotDate").value("2026-01-03"))
        .andExpect(jsonPath("$.topTags", hasSize(2)))
        .andExpect(jsonPath("$.topTags[0].tag").value("kubernetes"))
        .andExpect(jsonPath("$.topTags[0].signalCount").value(30));
  }
}
