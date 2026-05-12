import { useEffect, useMemo, useState } from "react";
import { ComparisonLineChart } from "../charts/ComparisonLineChart";
import { fetchDashboardData, type DashboardData } from "../services/api";

type LoadState =
  | { status: "loading"; data?: undefined; message?: undefined }
  | { status: "ready"; data: DashboardData; message?: undefined }
  | { status: "error"; data?: undefined; message: string };

const numberFormatter = new Intl.NumberFormat("en-US");
const dateFormatter = new Intl.DateTimeFormat("en-US", {
  month: "short",
  day: "numeric",
  year: "numeric",
});

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
          <a href="#summary">Summary</a>
          <a href="#rising">Rising</a>
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
            title="Spring Boot backend is not available"
            message={`${state.message} Start the backend with the demo profile, then refresh this dashboard.`}
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
  const latestSnapshotDate = formatDate(data.summary.mostRecentSnapshotDate);
  const topTechnology = data.summary.topTags[0]?.tag ?? "No data";

  return (
    <div className="dashboard-stack">
      <section className="panel panel-wide summary-panel" id="summary" aria-labelledby="summary-title">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">Trend Summary</p>
            <h2 id="summary-title">Current technology signal snapshot</h2>
            <p className="section-copy">
              A quick read on the latest transformed snapshot and the most active technologies in
              the local demo dataset.
            </p>
          </div>
        </div>
        <div className="metrics metrics-three" aria-label="Summary metrics">
          <Metric
            label="Latest snapshot date"
            value={latestSnapshotDate}
            help="Snapshot dates are normalized to UTC during transformation."
          />
          <Metric
            label="Total top-tag signals"
            value={numberFormatter.format(totalSignals)}
            help="Sum of signal counts for the current summary list."
          />
          <Metric
            label="Top technology"
            value={topTechnology}
            help="Highest signal count in the summary response."
          />
        </div>
        {data.summary.topTags.length === 0 ? (
          <EmptyState message="No summary metrics are available yet. Load demo data or run the orchestration pipeline to create trend snapshots." />
        ) : (
          <ol className="tag-list" aria-label="Top technologies by signal count">
            {data.summary.topTags.map((tag, index) => (
              <li key={tag.tag}>
                <span>
                  <strong className="rank">#{index + 1}</strong>
                  {tag.tag}
                </span>
                <strong>{numberFormatter.format(tag.signalCount)}</strong>
              </li>
            ))}
          </ol>
        )}
      </section>

      <div className="dashboard-grid">
        <section className="panel" id="rising" aria-labelledby="rising-title">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Rising Technologies</p>
              <h2 id="rising-title">Tags gaining momentum</h2>
              <p className="section-copy">
                Technologies are sorted by positive growth and rank movement between the latest
                snapshot and the previous comparison point.
              </p>
            </div>
          </div>
          {data.rising.length === 0 ? (
            <EmptyState message="No rising technologies are available yet. Run demo seeding or the orchestration pipeline to create transformed snapshots." />
          ) : (
            <div className="table-wrap">
              <table>
                <caption>
                  Rising tags with signal delta, percent change, and rank movement.
                </caption>
                <thead>
                  <tr>
                    <th scope="col">Tag</th>
                    <th scope="col">
                      <MetricHelp
                        label="Signal delta"
                        help="Current signal count minus previous signal count."
                      />
                    </th>
                    <th scope="col">
                      <MetricHelp
                        label="Percent change"
                        help="Percent growth when previous data is available and non-zero."
                      />
                    </th>
                    <th scope="col">
                      <MetricHelp
                        label="Rank movement"
                        help="Positive values mean the tag moved up in rank."
                      />
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {data.rising.map((trend) => (
                    <tr key={trend.tag}>
                      <td>
                        <strong>{trend.tag}</strong>
                      </td>
                      <td>
                        <span className={valueTone(trend.absoluteDelta)}>
                          {formatSigned(trend.absoluteDelta)}
                        </span>
                      </td>
                      <td>{formatPercent(trend.percentChange)}</td>
                      <td>{formatNullableSigned(trend.rankMovement)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="panel comparison-notes" aria-labelledby="comparison-notes-title">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Comparison Tags</p>
              <h2 id="comparison-notes-title">Demo tag coverage</h2>
              <p className="section-copy">
                The comparison API keeps missing tags visible so a demo can distinguish no local
                history from a failed request.
              </p>
            </div>
          </div>
          <ul className="comparison-list">
            {data.comparison.tags.map((tag) => (
              <li key={tag.normalizedTag}>
                <span>
                  <strong>{tag.normalizedTag}</strong>
                  <small>{tag.found ? "Snapshot history available" : "No local history yet"}</small>
                </span>
                <span className={tag.found ? "status-pill status-found" : "status-pill"}>
                  {tag.found ? "Found" : "Missing"}
                </span>
              </li>
            ))}
          </ul>
        </section>
      </div>

      <section className="panel panel-wide" id="comparison" aria-labelledby="comparison-title">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">Technology Comparison</p>
            <h2 id="comparison-title">Java, Python, and PostgreSQL over time</h2>
            <p className="section-copy">
              A chart-ready view of signal count history for common demo tags. Use it to discuss
              relative momentum, not absolute labor-market demand.
            </p>
          </div>
        </div>
        {data.comparison.tags.every((tag) => tag.history.length === 0) ? (
          <EmptyState message="Comparison history is empty. The API is healthy, but no snapshot history exists for the demo tags yet. Load demo data or run the transformation pipeline." />
        ) : (
          <ComparisonLineChart comparison={data.comparison} />
        )}
      </section>
    </div>
  );
}

function Metric({ label, value, help }: { label: string; value: string; help: string }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{help}</small>
    </div>
  );
}

function MetricHelp({ label, help }: { label: string; help: string }) {
  return (
    <span className="metric-help" title={help}>
      {label}
      <span aria-hidden="true">?</span>
    </span>
  );
}

function EmptyState({ message }: { message: string }) {
  return <p className="empty-state">{message}</p>;
}

function StatusPanel({ title, message }: { title: string; message?: string }) {
  return (
    <section className="panel status-panel" aria-live="polite">
      <h2>{title}</h2>
      <p>
        {message ??
          "Fetching the latest dashboard data from the local Spring Boot backend. This usually takes only a moment."}
      </p>
    </section>
  );
}

function formatDate(value: string | null) {
  if (value === null) {
    return "No data";
  }

  return dateFormatter.format(new Date(`${value}T00:00:00Z`));
}

function formatPercent(value: number | null) {
  return value === null ? "Not available" : `${value.toFixed(1)}%`;
}

function formatSigned(value: number) {
  return value > 0 ? `+${value}` : String(value);
}

function formatNullableSigned(value: number | null) {
  return value === null ? "Not available" : formatSigned(value);
}

function valueTone(value: number) {
  if (value > 0) {
    return "value-positive";
  }

  if (value < 0) {
    return "value-negative";
  }

  return "value-neutral";
}
