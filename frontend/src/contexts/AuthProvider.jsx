import { useMemo, useState } from 'react'
import { AuthContext } from './AuthContext'

function AuthProvider({ children }) {
  const [user, setUser] = useState(null)

  async function login({ email }) {
    const mockUser = {
      id: 'mock-user',
      email,
      name: email.split('@')[0] || 'User',
    }

    setUser(mockUser)
    return { ok: true, user: mockUser }
  }

  async function signup({ email }) {
    const mockUser = {
      id: 'mock-user',
      email,
      name: email.split('@')[0] || 'User',
    }

    setUser(mockUser)
    return { ok: true, user: mockUser }
  }

  function logout() {
    setUser(null)
  }

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(user),
      login,
      logout,
      signup,
      user,
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export default AuthProvider
