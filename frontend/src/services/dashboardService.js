import apiClient from './apiClient'

export async function getDashboardSummary() {
  const { data } = await apiClient.get('/dashboard/summary')
  return data
}

export async function getFinancialRecords({
  page = 0,
  size = 10,
  sort = 'transactionDate,desc',
} = {}) {
  const { data } = await apiClient.get('/records', {
    params: {
      page,
      size,
      sort,
    },
  })

  return data
}
