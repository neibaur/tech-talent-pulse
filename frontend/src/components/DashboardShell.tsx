import { useEffect, useMemo, useState } from "react";
import { fetchDashboardData, type DashboardData } from "../services/api";
import { ComparisonLineChart } from "../charts/ComparisonLineChart";

type LoadState =
  | { status: "loading"; data?: undefined; message?: undefined }
  | { status: "ready"; data: DashboardData; message?: undefined }
  | { status: "error"; data?: undefined; message: string };

const formatter = new Intl.NumberFormat("en-US");

export default function DashboardShell() {
  const [state, setState] = useState<LoadState>({ status: "loading" });

  useEffect(() => {
    let cancelled = false;

    fetchDashboardData()
      .then((data) => {
        if (!cancelled) {
          setState({ status: "ready", data });
        }
      })
      .catch((error: unknown) => {
        if (!cancelled) {
          const message =
            error instanceof Error
              ? error.message
              : "The analytics API could not be reached.";
          setState({ status: "error", message });
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="app-shell">
      <header className="site-header">
        <nav className="site-nav" aria-label="Primary">
          <span className="brand">Tech Talent Pulse</span>
          <a href="#rising">Rising</a>
          <a href="#summary">Summary</a>
          <a href="#comparison">Compare</a>
        </nav>
        <section className="intro">
          <p className="eyebrow">Recruiter intelligence dashboard</p>
          <h1>Technology trend signals, translated for hiring conversations.</h1>
          <p>
            A lightweight Astro view over the Spring Boot analytics APIs, focused on rising
            technologies, snapshot summaries, and chart-ready tag comparisons.
          </p>
        </section>
      </header>

      <main>
        {state.status === "loading" && <StatusPanel title="Loading analytics" />}
        {state.status === "error" && (
          <StatusPanel
            title="Backend API unavailable"
            message={`${state.message} Start the Spring Boot app locally and confirm the API URL is correct.`}
          />
        )}
        {state.status === "ready" && <DashboardContent data={state.data} />}
      </main>
    </div>
  );
}

function DashboardContent({ data }: { data: DashboardData }) {
  const totalSignals = useMemo(
    () =>
      data.summary.topTags.reduce((total, tag) => total + Number(tag.signalCount ?? 0), 0),
    [data.summary.topTags],
  );

  return (
    <div className="dashboard-grid">
      <section className="panel" id="rising">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">Growth signals</p>
            <h2>Rising Technologies</h2>
          </div>
        </div>
        {data.rising.length === 0 ? (
          <EmptyState message="No rising technologies are available yet. Run demo seeding or the orchestration pipeline to create transformed snapshots." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Tag</th>
                  <th>Delta</th>
                  <th>Change</th>
                  <th>Rank move</th>
                </tr>
              </thead>
              <tbody>
                {data.rising.map((trend) => (
                  <tr key={trend.tag}>
                    <td>{trend.tag}</td>
                    <td>{formatSigned(trend.absoluteDelta)}</td>
                    <td>{formatPercent(trend.percentChange)}</td>
                    <td>{formatNullableSigned(trend.rankMovement)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel" id="summary">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">Latest snapshot</p>
            <h2>Trend Summary</h2>
          </div>
        </div>
        <div className="metrics">
          <Metric label="Total top-tag signals" value={formatter.format(totalSignals)} />
          <Metric label="Latest snapshot" value={data.summary.mostRecentSnapshotDate ?? "No data"} />
        </div>
        {data.summary.topTags.length === 0 ? (
          <EmptyState message="No summary metrics are available yet." />
        ) : (
          <ol className="tag-list">
            {data.summary.topTags.map((tag) => (
              <li key={tag.tag}>
                <span>{tag.tag}</span>
                <strong>{formatter.format(tag.signalCount)}</strong>
              </li>
            ))}
          </ol>
        )}
      </section>

      <section className="panel panel-wide" id="comparison">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">Java vs Python vs PostgreSQL</p>
            <h2>Technology Comparison</h2>
          </div>
        </div>
        {data.comparison.tags.every((tag) => tag.history.length === 0) ? (
          <EmptyState message="Comparison history is empty. The API is healthy, but no snapshot history exists for the demo tags yet." />
        ) : (
          <ComparisonLineChart comparison={data.comparison} />
        )}
      </section>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return <p className="empty-state">{message}</p>;
}

function StatusPanel({ title, message }: { title: string; message?: string }) {
  return (
    <section className="panel status-panel" aria-live="polite">
      <h2>{title}</h2>
      <p>{message ?? "Fetching the latest dashboard data from the local backend."}</p>
    </section>
  );
}

function formatPercent(value: number | null) {
  return value === null ? "n/a" : `${value.toFixed(1)}%`;
}

function formatSigned(value: number) {
  return value > 0 ? `+${value}` : String(value);
}

function formatNullableSigned(value: number | null) {
  return value === null ? "n/a" : formatSigned(value);
}
