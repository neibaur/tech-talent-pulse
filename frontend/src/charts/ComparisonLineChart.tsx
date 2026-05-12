import {
  CartesianGrid,
  Label,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { TagComparisonResponse } from "../services/api";

const COLORS = ["#2563eb", "#0f766e", "#b45309", "#7c3aed", "#be123c"];
const numberFormatter = new Intl.NumberFormat("en-US");
const dateFormatter = new Intl.DateTimeFormat("en-US", {
  month: "short",
  day: "numeric",
});

interface Props {
  comparison: {
    tags: TagComparisonResponse[];
  };
}

export function ComparisonLineChart({ comparison }: Props) {
  const rows = buildRows(comparison.tags);

  return (
    <figure className="chart-frame" aria-label="Signal count history by technology tag">
      <figcaption>
        <strong>Signal count history</strong>
        <span>Higher points indicate more Stack Overflow signals captured for that tag.</span>
      </figcaption>
      <ResponsiveContainer width="100%" height={390}>
        <LineChart data={rows} margin={{ top: 20, right: 28, bottom: 34, left: 6 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#d7dee8" />
          <XAxis dataKey="snapshotDate" tickFormatter={formatDateTick} tickMargin={10} minTickGap={24}>
            <Label value="Snapshot date" position="insideBottom" offset={-22} fill="#617083" />
          </XAxis>
          <YAxis allowDecimals={false} tickFormatter={formatNumber} width={54}>
            <Label
              value="Signal count"
              angle={-90}
              position="insideLeft"
              offset={4}
              fill="#617083"
              style={{ textAnchor: "middle" }}
            />
          </YAxis>
          <Tooltip
            formatter={(value, name) => [formatNumber(Number(value)), String(name)]}
            labelFormatter={(label) => `Snapshot ${formatDateTick(String(label))}`}
          />
          <Legend verticalAlign="top" height={36} iconType="line" />
          {comparison.tags.map((tag, index) => (
            <Line
              key={tag.normalizedTag}
              type="monotone"
              dataKey={tag.normalizedTag}
              name={tag.normalizedTag}
              stroke={COLORS[index % COLORS.length]}
              strokeWidth={2.5}
              dot={{ r: 3, strokeWidth: 1.5 }}
              activeDot={{ r: 5 }}
              connectNulls
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </figure>
  );
}

function buildRows(tags: TagComparisonResponse[]) {
  const rowsByDate = new Map<string, Record<string, string | number | null>>();

  for (const tag of tags) {
    for (const point of tag.history) {
      const row = rowsByDate.get(point.snapshotDate) ?? { snapshotDate: point.snapshotDate };
      row[tag.normalizedTag] = point.signalCount;
      rowsByDate.set(point.snapshotDate, row);
    }
  }

  return Array.from(rowsByDate.values()).sort((left, right) =>
    String(left.snapshotDate).localeCompare(String(right.snapshotDate)),
  );
}

function formatDateTick(value: string) {
  return dateFormatter.format(new Date(`${value}T00:00:00Z`));
}

function formatNumber(value: number) {
  return numberFormatter.format(value);
}
