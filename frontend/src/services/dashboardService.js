import apiClient from './apiClient'
import { useMockApi } from './runtimeConfig'

const mockSummary = {
  totalIncome: 15850,
  totalExpense: 9420,
  netBalance: 6430,
  categoryWiseTotals: {
    Housing: 3200,
    Transport: 1280,
    Food: 2140,
    Utilities: 860,
    Entertainment: 760,
    Other: 1180,
  },
  monthlySummary: {
    '2025-10': 950,
    '2025-11': 1110,
    '2025-12': 820,
    '2026-01': 1275,
    '2026-02': 1040,
    '2026-03': 1235,
  },
  weeklyTrends: {
    Mon: 240,
    Tue: 180,
    Wed: 210,
    Thu: 265,
    Fri: 295,
    Sat: 160,
    Sun: 130,
  },
}

const mockRecords = [
  {
    id: 1,
    title: 'Monthly Salary',
    category: 'Income',
    type: 'INCOME',
    amount: 4500,
    transactionDate: '2026-03-31',
  },
  {
    id: 2,
    title: 'Rent Payment',
    category: 'Housing',
    type: 'EXPENSE',
    amount: 1600,
    transactionDate: '2026-03-28',
  },
  {
    id: 3,
    title: 'Freelance Invoice',
    category: 'Income',
    type: 'INCOME',
    amount: 1200,
    transactionDate: '2026-03-24',
  },
  {
    id: 4,
    title: 'Groceries',
    category: 'Food',
    type: 'EXPENSE',
    amount: 190,
    transactionDate: '2026-03-22',
  },
  {
    id: 5,
    title: 'Electricity Bill',
    category: 'Utilities',
    type: 'EXPENSE',
    amount: 125,
    transactionDate: '2026-03-20',
  },
  {
    id: 6,
    title: 'Fuel',
    category: 'Transport',
    type: 'EXPENSE',
    amount: 95,
    transactionDate: '2026-03-19',
  },
  {
    id: 7,
    title: 'Dining',
    category: 'Food',
    type: 'EXPENSE',
    amount: 74,
    transactionDate: '2026-03-16',
  },
  {
    id: 8,
    title: 'Streaming Subscription',
    category: 'Entertainment',
    type: 'EXPENSE',
    amount: 18,
    transactionDate: '2026-03-15',
  },
  {
    id: 9,
    title: 'Taxi Reimbursement',
    category: 'Transport',
    type: 'INCOME',
    amount: 48,
    transactionDate: '2026-03-11',
  },
  {
    id: 10,
    title: 'Gym Membership',
    category: 'Health',
    type: 'EXPENSE',
    amount: 55,
    transactionDate: '2026-03-10',
  },
  {
    id: 11,
    title: 'Bonus',
    category: 'Income',
    type: 'INCOME',
    amount: 1600,
    transactionDate: '2026-03-08',
  },
  {
    id: 12,
    title: 'Insurance',
    category: 'Insurance',
    type: 'EXPENSE',
    amount: 310,
    transactionDate: '2026-03-03',
  },
]

export async function getDashboardSummary() {
  if (useMockApi) {
    return Promise.resolve(mockSummary)
  }

  const { data } = await apiClient.get('/dashboard/summary')
  return data
}

export async function getFinancialRecords({
  page = 0,
  size = 10,
  sort = 'transactionDate,desc',
} = {}) {
  if (useMockApi) {
    const startIndex = page * size
    const endIndex = startIndex + size
    const content = mockRecords.slice(startIndex, endIndex)
    const totalPages = Math.max(1, Math.ceil(mockRecords.length / size))

    return Promise.resolve({
      content,
      number: page,
      totalPages,
      totalElements: mockRecords.length,
      size,
      sort: {
        sorted: true,
      },
    })
  }

  const { data } = await apiClient.get('/records', {
    params: {
      page,
      size,
      sort,
    },
  })

  return data
}
