import apiClient from './apiClient'

export async function login(credentials) {
  const { data } = await apiClient.post('/auth/login', credentials)
  return data
}

export async function register(payload) {
  const { data } = await apiClient.post('/auth/register', payload)
  return data
}

export async function logout() {
  const { data } = await apiClient.post('/auth/logout')
  return data
}
