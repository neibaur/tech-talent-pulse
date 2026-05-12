package com.techtalentpulse.analytics.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.techtalentpulse.analytics.application.TrendAnalyticsService;
import com.techtalentpulse.analytics.domain.TrendDelta;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TrendAnalyticsController.class)
class TrendAnalyticsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TrendAnalyticsService trendAnalyticsService;

  @Test
  void returnsTrendDeltas() throws Exception {
    when(trendAnalyticsService.trendDeltas(5))
        .thenReturn(List.of(delta("java", 20, 10, 10, 100.0, 1, 2, 1)));

    mockMvc
        .perform(get("/api/analytics/trends/deltas").param("limit", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].tag").value("java"))
        .andExpect(jsonPath("$[0].currentSnapshotDate").value("2026-01-03"))
        .andExpect(jsonPath("$[0].previousSnapshotDate").value("2026-01-02"))
        .andExpect(jsonPath("$[0].currentSignalCount").value(20))
        .andExpect(jsonPath("$[0].previousSignalCount").value(10))
        .andExpect(jsonPath("$[0].absoluteDelta").value(10))
        .andExpect(jsonPath("$[0].percentChange").value(100.0))
        .andExpect(jsonPath("$[0].currentRank").value(1))
        .andExpect(jsonPath("$[0].previousRank").value(2))
        .andExpect(jsonPath("$[0].rankMovement").value(1));
  }

  @Test
  void returnsRisingTrends() throws Exception {
    when(trendAnalyticsService.risingTrends(null))
        .thenReturn(List.of(delta("postgresql", 16, 6, 10, 166.6666666667, 2, 3, 1)));

    mockMvc
        .perform(get("/api/analytics/trends/rising"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].tag").value("postgresql"))
        .andExpect(jsonPath("$[0].absoluteDelta").value(10));
  }

  private TrendDelta delta(
      String tag,
      int currentSignalCount,
      int previousSignalCount,
      int absoluteDelta,
      Double percentChange,
      int currentRank,
      Integer previousRank,
      Integer rankMovement) {
    return new TrendDelta(
        tag,
        LocalDate.parse("2026-01-03"),
        LocalDate.parse("2026-01-02"),
        currentSignalCount,
        previousSignalCount,
        absoluteDelta,
        percentChange,
        currentRank,
        previousRank,
        rankMovement);
  }
}
