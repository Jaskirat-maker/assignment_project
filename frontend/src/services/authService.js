import apiClient from './apiClient'
import { useMockApi } from './runtimeConfig'

function mockToken(prefix) {
  return `${prefix}_${Math.random().toString(36).slice(2, 10)}`
}

export async function login(credentials) {
  if (useMockApi) {
    return {
      token: mockToken('access'),
      refreshToken: mockToken('refresh'),
      username: credentials.username,
      email: `${credentials.username}@example.com`,
      role: 'ANALYST',
    }
  }

  const { data } = await apiClient.post('/auth/login', credentials)
  return data
}

export async function register(payload) {
  if (useMockApi) {
    return {
      token: mockToken('access'),
      refreshToken: mockToken('refresh'),
      username: payload.username,
      email: payload.email,
      role: 'ANALYST',
    }
  }

  const { data } = await apiClient.post('/auth/register', payload)
  return data
}

export async function logout() {
  if (useMockApi) {
    return 'Logged out successfully'
  }

  const { data } = await apiClient.post('/auth/logout')
  return data
}
