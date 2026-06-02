import { useEffect, useMemo, useState } from 'react'
import api from '../api/client'
import { AuthContext } from './AuthContext'

function errorMessageFor(error, fallback) {
  return (
    error.response?.data?.detail ||
    error.response?.data?.message ||
    error.response?.data?.error ||
    fallback
  )
}

function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [authLoading, setAuthLoading] = useState(true)

  useEffect(() => {
    let ignore = false

    async function loadSessionUser() {
      try {
        const response = await api.get('/api/auth/me')

        if (!ignore) {
          setUser(response.data)
        }
      } catch {
        if (!ignore) {
          setUser(null)
        }
      } finally {
        if (!ignore) {
          setAuthLoading(false)
        }
      }
    }

    function handleUnauthorized() {
      setUser(null)
    }

    window.addEventListener('iis:unauthorized', handleUnauthorized)
    loadSessionUser()

    return () => {
      ignore = true
      window.removeEventListener('iis:unauthorized', handleUnauthorized)
    }
  }, [])

  async function login({ email, password }) {
    try {
      const response = await api.post('/api/auth/login', { email, password })

      setUser(response.data)

      return { ok: true, user: response.data }
    } catch (error) {
      return {
        ok: false,
        message: errorMessageFor(error, 'Unable to log in right now'),
      }
    }
  }

  async function signup({ email, password }) {
    try {
      const response = await api.post('/api/auth/signup', { email, password })

      setUser(response.data)

      return { ok: true, user: response.data }
    } catch (error) {
      return {
        ok: false,
        message: errorMessageFor(error, 'Unable to create account right now'),
      }
    }
  }

  async function logout() {
    try {
      await api.post('/api/auth/logout')
    } finally {
      setUser(null)
    }
  }

  function updateUser(nextUser) {
    setUser(nextUser)
  }

  const value = useMemo(
    () => ({
      authLoading,
      isAuthenticated: Boolean(user),
      login,
      logout,
      signup,
      updateUser,
      user,
    }),
    [authLoading, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export default AuthProvider
