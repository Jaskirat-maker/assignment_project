import { createContext, useContext, useMemo, useState } from 'react'
import { login, logout, register } from '../services/authService'
import { clearStoredSession, getStoredSession, saveStoredSession } from '../utils/storage'

const AuthContext = createContext(null)

function toSession(payload) {
  return {
    token: payload.token,
    refreshToken: payload.refreshToken,
    username: payload.username,
    email: payload.email,
    role: payload.role,
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => getStoredSession())

  const setAuthSession = (nextSession) => {
    setSession(nextSession)

    if (nextSession) {
      saveStoredSession(nextSession)
      return
    }

    clearStoredSession()
  }

  const handleLogin = async (credentials) => {
    const response = await login(credentials)
    const nextSession = toSession(response)
    setAuthSession(nextSession)
    return nextSession
  }

  const handleRegister = async (payload) => {
    const response = await register(payload)
    const nextSession = toSession(response)
    setAuthSession(nextSession)
    return nextSession
  }

  const handleLogout = async () => {
    try {
      await logout()
    } catch {
      // Token may be expired or invalid, clear local session regardless.
    }

    setAuthSession(null)
  }

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
    [session],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }

  return context
}
