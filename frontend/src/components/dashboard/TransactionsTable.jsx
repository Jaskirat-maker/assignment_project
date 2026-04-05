import { formatCurrency, formatDate } from '../../utils/formatters'

const typeClassName = {
  INCOME: 'bg-emerald-100 text-emerald-700',
  EXPENSE: 'bg-rose-100 text-rose-700',
}

export default function TransactionsTable({
  records,
  page,
  totalPages,
  loading,
  onPreviousPage,
  onNextPage,
}) {
  return (
    <article className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-lg font-semibold text-slate-900">Transactions</h2>
        <p className="text-sm text-slate-500">Page {page + 1} of {Math.max(totalPages, 1)}</p>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
          <thead>
            <tr className="text-slate-500">
              <th className="px-3 py-2 font-semibold">Title</th>
              <th className="px-3 py-2 font-semibold">Category</th>
              <th className="px-3 py-2 font-semibold">Type</th>
              <th className="px-3 py-2 font-semibold">Date</th>
              <th className="px-3 py-2 text-right font-semibold">Amount</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {loading ? (
              <tr>
                <td colSpan={5} className="px-3 py-8 text-center text-slate-500">Loading transactions...</td>
              </tr>
            ) : records.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-3 py-8 text-center text-slate-500">No transactions found.</td>
              </tr>
            ) : (
              records.map((record) => (
                <tr key={record.id} className="text-slate-700">
                  <td className="px-3 py-3">{record.title}</td>
                  <td className="px-3 py-3">{record.category || '-'}</td>
                  <td className="px-3 py-3">
                    <span
                      className={`inline-flex rounded-full px-2 py-1 text-xs font-semibold ${
                        typeClassName[record.type] || 'bg-slate-100 text-slate-600'
                      }`}
                    >
                      {record.type}
                    </span>
                  </td>
                  <td className="px-3 py-3">{formatDate(record.transactionDate)}</td>
                  <td className="px-3 py-3 text-right font-semibold">{formatCurrency(record.amount)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="mt-4 flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={onPreviousPage}
          disabled={loading || page === 0}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Previous
        </button>
        <button
          type="button"
          onClick={onNextPage}
          disabled={loading || page + 1 >= totalPages}
          className="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </article>
  )
}
