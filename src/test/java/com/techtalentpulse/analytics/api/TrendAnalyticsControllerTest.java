package com.techtalentpulse.analytics.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.techtalentpulse.analytics.application.TrendAnalyticsService;
import com.techtalentpulse.analytics.domain.TagTrendComparison;
import com.techtalentpulse.analytics.domain.TagTrendComparisonResult;
import com.techtalentpulse.analytics.domain.TagTrendDeltaMetrics;
import com.techtalentpulse.analytics.domain.TagTrendHistoryPoint;
import com.techtalentpulse.analytics.domain.TagTrendLatestMetrics;
import com.techtalentpulse.analytics.domain.TrendDelta;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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

  @Test
  void returnsTagComparisonResponse() throws Exception {
    when(trendAnalyticsService.compareTags(List.of("java", " python ")))
        .thenReturn(
            new TagTrendComparisonResult(
                List.of(
                    new TagTrendComparison(
                        "java",
                        "java",
                        true,
                        new TagTrendLatestMetrics(LocalDate.parse("2026-01-03"), 20, 4.5, 2.0, 1),
                        new TagTrendDeltaMetrics(
                            LocalDate.parse("2026-01-02"), 10, 10, 100.0, 2, 1),
                        List.of(
                            new TagTrendHistoryPoint(LocalDate.parse("2026-01-03"), 20, 4.5, 2.0))),
                    new TagTrendComparison("python", "python", false, null, null, List.of()))));

    mockMvc
        .perform(get("/api/analytics/trends/compare").param("tags", "java, python "))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tags", hasSize(2)))
        .andExpect(jsonPath("$.tags[0].requestedTag").value("java"))
        .andExpect(jsonPath("$.tags[0].normalizedTag").value("java"))
        .andExpect(jsonPath("$.tags[0].found").value(true))
        .andExpect(jsonPath("$.tags[0].latestMetrics.snapshotDate").value("2026-01-03"))
        .andExpect(jsonPath("$.tags[0].latestMetrics.signalCount").value(20))
        .andExpect(jsonPath("$.tags[0].latestMetrics.averageScore").value(4.5))
        .andExpect(jsonPath("$.tags[0].latestMetrics.averageAnswerCount").value(2.0))
        .andExpect(jsonPath("$.tags[0].latestMetrics.currentRank").value(1))
        .andExpect(jsonPath("$.tags[0].deltaMetrics.previousSignalCount").value(10))
        .andExpect(jsonPath("$.tags[0].deltaMetrics.absoluteDelta").value(10))
        .andExpect(jsonPath("$.tags[0].deltaMetrics.percentChange").value(100.0))
        .andExpect(jsonPath("$.tags[0].history", hasSize(1)))
        .andExpect(jsonPath("$.tags[1].found").value(false))
        .andExpect(jsonPath("$.tags[1].latestMetrics").doesNotExist());
  }

  @Test
  void returnsBadRequestForMissingCompareTags() throws Exception {
    mockMvc.perform(get("/api/analytics/trends/compare")).andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/api/analytics/trends/compare").param("tags", " "))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returnsBadRequestForInvalidCompareTagCounts() throws Exception {
    when(trendAnalyticsService.compareTags(anyList()))
        .thenThrow(new IllegalArgumentException("invalid tag count"));

    mockMvc
        .perform(get("/api/analytics/trends/compare").param("tags", "java"))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(get("/api/analytics/trends/compare").param("tags", "a,b,c,d,e,f"))
        .andExpect(status().isBadRequest());
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
