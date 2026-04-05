import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { formatCurrency } from '../../utils/formatters'

const CHART_COLORS = ['#4f46e5', '#0ea5e9', '#14b8a6', '#22c55e', '#f59e0b', '#f97316']

function moneyTooltip(value) {
  return formatCurrency(value)
}

export default function ChartsSection({ summary }) {
  const categoryData = Object.entries(summary?.categoryWiseTotals ?? {}).map(
    ([name, value]) => ({
      name,
      value: Number(value),
    }),
  )

  const monthlyData = Object.entries(summary?.monthlySummary ?? {})
    .map(([month, total]) => ({
      month,
      total: Number(total),
    }))
    .sort((first, second) => first.month.localeCompare(second.month))

  const weeklyData = Object.entries(summary?.weeklyTrends ?? {}).map(([day, total]) => ({
    day,
    total: Number(total),
  }))

  return (
    <section className="grid gap-4 xl:grid-cols-3">
      <article className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm xl:col-span-1">
        <h2 className="text-lg font-semibold text-slate-900">Category Split</h2>
        <div className="mt-4 h-64">
          {categoryData.length ? (
            <ResponsiveContainer>
              <PieChart>
                <Pie data={categoryData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={85}>
                  {categoryData.map((entry, index) => (
                    <Cell key={`cell-${entry.name}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={moneyTooltip} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-full items-center justify-center text-sm text-slate-500">No category data</div>
          )}
        </div>
      </article>

      <article className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm xl:col-span-2">
        <h2 className="text-lg font-semibold text-slate-900">Monthly Summary</h2>
        <div className="mt-4 h-64">
          {monthlyData.length ? (
            <ResponsiveContainer>
              <LineChart data={monthlyData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip formatter={moneyTooltip} />
                <Line type="monotone" dataKey="total" stroke="#4f46e5" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-full items-center justify-center text-sm text-slate-500">No monthly summary</div>
          )}
        </div>
      </article>

      <article className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm xl:col-span-3">
        <h2 className="text-lg font-semibold text-slate-900">Weekly Trends</h2>
        <div className="mt-4 h-64">
          {weeklyData.length ? (
            <ResponsiveContainer>
              <BarChart data={weeklyData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip formatter={moneyTooltip} />
                <Bar dataKey="total" fill="#0ea5e9" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-full items-center justify-center text-sm text-slate-500">No weekly trends</div>
          )}
        </div>
      </article>
    </section>
  )
}
