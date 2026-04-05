import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import ChartsSection from '../components/dashboard/ChartsSection'
import SummaryCards from '../components/dashboard/SummaryCards'
import TransactionsTable from '../components/dashboard/TransactionsTable'
import AppLayout from '../components/layout/AppLayout'
import useAuth from '../context/useAuth'
import {
  getDashboardSummary,
  getFinancialRecords,
} from '../services/dashboardService'
import { getApiErrorMessage } from '../utils/http'

export default function DashboardPage() {
  const navigate = useNavigate()
  const { logout, user } = useAuth()

  const [summary, setSummary] = useState(null)
  const [recordsPage, setRecordsPage] = useState({
    content: [],
    number: 0,
    totalPages: 0,
  })
  const [isSummaryLoading, setIsSummaryLoading] = useState(true)
  const [isRecordsLoading, setIsRecordsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  const loadSummary = useCallback(async () => {
    setIsSummaryLoading(true)

    try {
      const response = await getDashboardSummary()
      setSummary(response)
    } finally {
      setIsSummaryLoading(false)
    }
  }, [])

  const loadRecords = useCallback(async (nextPage = 0) => {
    setIsRecordsLoading(true)

    try {
      const response = await getFinancialRecords({ page: nextPage })
      setRecordsPage(response)
    } finally {
      setIsRecordsLoading(false)
    }
  }, [])

  useEffect(() => {
    async function bootstrapData() {
      try {
        setErrorMessage('')
        await Promise.all([loadSummary(), loadRecords(0)])
      } catch (error) {
        setErrorMessage(getApiErrorMessage(error, 'Failed to load dashboard data.'))
      }
    }

    bootstrapData()
  }, [loadRecords, loadSummary])

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  const handlePreviousPage = () => {
    if (recordsPage.number > 0) {
      loadRecords(recordsPage.number - 1)
    }
  }

  const handleNextPage = () => {
    if (recordsPage.number + 1 < recordsPage.totalPages) {
      loadRecords(recordsPage.number + 1)
    }
  }

  return (
    <AppLayout user={user} onLogout={handleLogout}>
      <div className="space-y-6">
        <header>
          <h2 className="text-3xl font-bold text-slate-900">Finance overview</h2>
          <p className="mt-1 text-slate-500">
            Track balances, trends, and transactions from your Spring Boot API.
          </p>
        </header>

        {errorMessage ? (
          <p className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
            {errorMessage}
          </p>
        ) : null}

        {isSummaryLoading ? (
          <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500">
            Loading summary...
          </div>
        ) : (
          <>
            <SummaryCards summary={summary} />
            <ChartsSection summary={summary} />
          </>
        )}

        <TransactionsTable
          records={recordsPage.content ?? []}
          page={recordsPage.number ?? 0}
          totalPages={recordsPage.totalPages ?? 0}
          loading={isRecordsLoading}
          onPreviousPage={handlePreviousPage}
          onNextPage={handleNextPage}
        />
      </div>
    </AppLayout>
  )
}
