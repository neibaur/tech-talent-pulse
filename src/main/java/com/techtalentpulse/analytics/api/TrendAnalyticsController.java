package com.techtalentpulse.analytics.api;

import com.techtalentpulse.analytics.application.TrendAnalyticsService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/analytics/trends")
public class TrendAnalyticsController {

  private final TrendAnalyticsService trendAnalyticsService;

  public TrendAnalyticsController(TrendAnalyticsService trendAnalyticsService) {
    this.trendAnalyticsService = trendAnalyticsService;
  }

  @GetMapping("/deltas")
  public ResponseEntity<List<TrendDeltaResponse>> deltas(
      @RequestParam(required = false) Integer limit) {
    List<TrendDeltaResponse> response =
        trendAnalyticsService.trendDeltas(limit).stream().map(TrendDeltaResponse::from).toList();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/rising")
  public ResponseEntity<List<TrendDeltaResponse>> rising(
      @RequestParam(required = false) Integer limit) {
    List<TrendDeltaResponse> response =
        trendAnalyticsService.risingTrends(limit).stream().map(TrendDeltaResponse::from).toList();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/compare")
  public ResponseEntity<TagTrendComparisonResponse> compare(
      @RequestParam(required = false) String tags) {
    try {
      return ResponseEntity.ok(
          TagTrendComparisonResponse.from(trendAnalyticsService.compareTags(parseTags(tags))));
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
  }

  private List<String> parseTags(String tags) {
    if (tags == null || tags.trim().isEmpty()) {
      throw new IllegalArgumentException("At least two tags are required.");
    }
    return List.of(tags.split(","));
  }
}
