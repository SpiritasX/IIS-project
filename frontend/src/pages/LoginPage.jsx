import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthButton from '../components/auth/AuthButton'
import AuthCard from '../components/auth/AuthCard'
import AuthInput from '../components/auth/AuthInput'
import { useAuth } from '../hooks/useAuth'
import '../styles/auth.css'

const initialValues = {
  email: '',
  password: '',
}

function validate(values) {
  const errors = {}

  if (!values.email.trim()) {
    errors.email = 'Email is required'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
    errors.email = 'Enter a valid email address'
  }

  if (!values.password) {
    errors.password = 'Password is required'
  }

  return errors
}

function LoginPage() {
  const { isAuthenticated, login, user } = useAuth()
  const navigate = useNavigate()
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})
  const [submitted, setSubmitted] = useState(false)

  function handleChange(event) {
    const { name, value } = event.target

    setValues((current) => ({
      ...current,
      [name]: value,
    }))

    if (errors[name]) {
      setErrors((current) => ({
        ...current,
        [name]: '',
      }))
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()

    const nextErrors = validate(values)
    setErrors(nextErrors)
    setSubmitted(true)

    if (Object.keys(nextErrors).length > 0) {
      return
    }

    await login({ email: values.email, password: values.password })
    navigate('/home')
  }

  return (
    <main className="login-page">
      <AuthCard>
        <h1 className="auth-title">Login</h1>

        <form className="auth-form" noValidate onSubmit={handleSubmit}>
          <AuthInput
            error={submitted ? errors.email : ''}
            helperText="enter email address"
            id="email"
            label="Email"
            name="email"
            onChange={handleChange}
            type="email"
            value={values.email}
          />

          <AuthInput
            error={submitted ? errors.password : ''}
            helperText="forgot password"
            id="password"
            label="Password"
            name="password"
            onChange={handleChange}
            type="password"
            value={values.password}
          />

          <AuthButton type="submit">Log in</AuthButton>
        </form>

        {isAuthenticated ? (
          <p className="auth-status" role="status">
            Signed in as {user.email}
          </p>
        ) : (
          <Link className="auth-link" to="/signup">
            Don&apos;t have an account? Sign up!
          </Link>
        )}
      </AuthCard>
    </main>
  )
}

export default LoginPage
