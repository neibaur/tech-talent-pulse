const API_BASE_URL =
  import.meta.env.PUBLIC_TECH_TALENT_PULSE_API_URL?.replace(/\/$/, "") ??
  "http://localhost:8080";

export interface TrendDeltaResponse {
  tag: string;
  currentSnapshotDate: string;
  previousSnapshotDate: string | null;
  currentSignalCount: number;
  previousSignalCount: number;
  absoluteDelta: number;
  percentChange: number | null;
  currentRank: number;
  previousRank: number | null;
  rankMovement: number | null;
}

export interface TrendSummaryResponse {
  mostRecentSnapshotDate: string | null;
  topTags: TopTagTrendResponse[];
}

export interface TopTagTrendResponse {
  tag: string;
  signalCount: number;
}

export interface TagTrendComparisonResponse {
  tags: TagComparisonResponse[];
}

export interface TagComparisonResponse {
  requestedTag: string;
  normalizedTag: string;
  found: boolean;
  latestMetrics: TagTrendLatestMetricsResponse | null;
  deltaMetrics: TagTrendDeltaMetricsResponse | null;
  history: TagTrendHistoryPointResponse[];
}

export interface TagTrendLatestMetricsResponse {
  snapshotDate: string;
  signalCount: number;
  averageScore: number;
  averageAnswerCount: number;
  currentRank: number | null;
}

export interface TagTrendDeltaMetricsResponse {
  previousSnapshotDate: string | null;
  previousSignalCount: number;
  absoluteDelta: number;
  percentChange: number | null;
  previousRank: number | null;
  rankMovement: number | null;
}

export interface TagTrendHistoryPointResponse {
  snapshotDate: string;
  signalCount: number;
  averageScore: number;
  averageAnswerCount: number;
}

export interface DashboardData {
  rising: TrendDeltaResponse[];
  summary: TrendSummaryResponse;
  comparison: TagTrendComparisonResponse;
}

export async function fetchDashboardData(): Promise<DashboardData> {
  const [rising, summary, comparison] = await Promise.all([
    getJson<TrendDeltaResponse[]>("/api/analytics/trends/rising"),
    getJson<TrendSummaryResponse>("/api/trends/summary"),
    getJson<TagTrendComparisonResponse>(
      "/api/analytics/trends/compare?tags=java,python,postgresql",
    ),
  ]);

  return { rising, summary, comparison };
}

async function getJson<T>(path: string): Promise<T> {
  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`);
  } catch (error) {
    throw new Error(
      "The dashboard could not connect to the local analytics API. Confirm the Spring Boot backend is running and the frontend API URL is correct.",
      { cause: error },
    );
  }

  if (!response.ok) {
    throw new Error(
      "The analytics API responded unexpectedly. Confirm the backend is healthy and try refreshing the dashboard.",
    );
  }

  return response.json() as Promise<T>;
}
