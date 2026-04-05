import { useCallback, useMemo, useState } from 'react'
import AuthContext from './AuthContext'
import { login, logout, register } from '../services/authService'
import { clearStoredSession, getStoredSession, saveStoredSession } from '../utils/storage'

function toSession(payload) {
  return {
    token: payload.token,
    refreshToken: payload.refreshToken,
    username: payload.username,
    email: payload.email,
    role: payload.role,
  }
}

export default function AuthProvider({ children }) {
  const [session, setSession] = useState(() => getStoredSession())

  const setAuthSession = useCallback((nextSession) => {
    setSession(nextSession)

    if (nextSession) {
      saveStoredSession(nextSession)
      return
    }

    clearStoredSession()
  }, [])

  const handleLogin = useCallback(
    async (credentials) => {
      const response = await login(credentials)
      const nextSession = toSession(response)
      setAuthSession(nextSession)
      return nextSession
    },
    [setAuthSession],
  )

  const handleRegister = useCallback(
    async (payload) => {
      const response = await register(payload)
      const nextSession = toSession(response)
      setAuthSession(nextSession)
      return nextSession
    },
    [setAuthSession],
  )

  const handleLogout = useCallback(async () => {
    try {
      await logout()
    } catch {
      // Token may be expired or invalid, clear local session regardless.
    }

    setAuthSession(null)
  }, [setAuthSession])

  const value = useMemo(
    () => ({
      authenticated: Boolean(session?.token),
      session,
      user: session
        ? {
            username: session.username,
            email: session.email,
            role: session.role,
          }
        : null,
      login: handleLogin,
      register: handleRegister,
      logout: handleLogout,
    }),
    [handleLogin, handleLogout, handleRegister, session],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
