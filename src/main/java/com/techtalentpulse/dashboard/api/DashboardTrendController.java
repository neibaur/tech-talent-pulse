package com.techtalentpulse.dashboard.api;

import com.techtalentpulse.dashboard.application.DashboardTrendService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trends")
public class DashboardTrendController {

  private final DashboardTrendService dashboardTrendService;

  public DashboardTrendController(DashboardTrendService dashboardTrendService) {
    this.dashboardTrendService = dashboardTrendService;
  }

  @GetMapping
  public ResponseEntity<List<TrendSnapshotResponse>> recentTrends(
      @RequestParam(required = false) Integer limit) {
    List<TrendSnapshotResponse> response =
        dashboardTrendService.recentSnapshots(limit).stream()
            .map(TrendSnapshotResponse::from)
            .toList();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/summary")
  public ResponseEntity<TrendSummaryResponse> summary(
      @RequestParam(required = false) Integer limit) {
    return ResponseEntity.ok(TrendSummaryResponse.from(dashboardTrendService.summary(limit)));
  }

  @GetMapping("/{tag}")
  public ResponseEntity<List<TrendSnapshotResponse>> trendsByTag(
      @PathVariable String tag, @RequestParam(required = false) Integer limit) {
    List<TrendSnapshotResponse> response =
        dashboardTrendService.trendHistory(tag, limit).stream()
            .map(TrendSnapshotResponse::from)
            .toList();
    return ResponseEntity.ok(response);
  }
}
