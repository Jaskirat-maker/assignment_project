import axios from 'axios'
import {
  clearStoredSession,
  getStoredSession,
  saveStoredSession,
} from '../utils/storage'
import { runtimeConfig } from './runtimeConfig'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  if (runtimeConfig.useMockApi) {
    return config
  }

  const session = getStoredSession()

  if (session?.token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${session.token}`
  }

  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (runtimeConfig.useMockApi) {
      throw error
    }

    const originalRequest = error.config
    const session = getStoredSession()

    const shouldTryRefresh =
      error.response?.status === 401 &&
      session?.refreshToken &&
      !originalRequest?._retry &&
      !originalRequest?.url?.includes('/auth/refresh')

    if (!shouldTryRefresh) {
      throw error
    }

    originalRequest._retry = true

    try {
      const { data } = await axios.post(
        `${API_BASE_URL}/auth/refresh`,
        { refreshToken: session.refreshToken },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        },
      )

      const refreshedSession = {
        ...session,
        token: data.token,
        refreshToken: data.refreshToken ?? session.refreshToken,
      }

      saveStoredSession(refreshedSession)
      originalRequest.headers = originalRequest.headers ?? {}
      originalRequest.headers.Authorization = `Bearer ${refreshedSession.token}`

      return apiClient(originalRequest)
    } catch (refreshError) {
      clearStoredSession()
      throw refreshError
    }
  },
)

export default apiClient
