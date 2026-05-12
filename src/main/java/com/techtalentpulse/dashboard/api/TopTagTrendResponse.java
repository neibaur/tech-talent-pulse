package com.techtalentpulse.dashboard.api;

import com.techtalentpulse.dashboard.domain.TopTagTrend;

public record TopTagTrendResponse(String tag, long signalCount) {

  static TopTagTrendResponse from(TopTagTrend topTag) {
    return new TopTagTrendResponse(topTag.tag(), topTag.signalCount());
  }
}
