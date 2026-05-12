import {
  CartesianGrid,
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

interface Props {
  comparison: {
    tags: TagComparisonResponse[];
  };
}

export function ComparisonLineChart({ comparison }: Props) {
  const rows = buildRows(comparison.tags);

  return (
    <div className="chart-frame">
      <ResponsiveContainer width="100%" height={360}>
        <LineChart data={rows} margin={{ top: 16, right: 24, bottom: 8, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#d7dee8" />
          <XAxis dataKey="snapshotDate" tickMargin={10} minTickGap={24} />
          <YAxis allowDecimals={false} width={48} />
          <Tooltip />
          <Legend />
          {comparison.tags.map((tag, index) => (
            <Line
              key={tag.normalizedTag}
              type="monotone"
              dataKey={tag.normalizedTag}
              name={tag.normalizedTag}
              stroke={COLORS[index % COLORS.length]}
              strokeWidth={2}
              dot={{ r: 3 }}
              connectNulls
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
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
