import { formatCurrency } from '../../utils/formatters'

export default function SummaryCards({ summary }) {
  const cards = [
    {
      title: 'Total Income',
      value: summary?.totalIncome,
      valueClassName: 'text-emerald-600',
    },
    {
      title: 'Total Expense',
      value: summary?.totalExpense,
      valueClassName: 'text-rose-600',
    },
    {
      title: 'Net Balance',
      value: summary?.netBalance,
      valueClassName: 'text-indigo-700',
    },
  ]

  return (
    <section className="grid gap-4 md:grid-cols-3">
      {cards.map((card) => (
        <article
          key={card.title}
          className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
        >
          <p className="text-sm font-medium text-slate-500">{card.title}</p>
          <p className={`mt-2 text-2xl font-bold ${card.valueClassName}`}>
            {formatCurrency(card.value)}
          </p>
        </article>
      ))}
    </section>
  )
}
