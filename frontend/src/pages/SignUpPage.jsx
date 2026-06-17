import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthButton from '../components/auth/AuthButton'
import AuthCard from '../components/auth/AuthCard'
import AuthInput from '../components/auth/AuthInput'
import { useAuth } from '../hooks/useAuth'
import { homePathForRole } from '../utils/roleRoutes'
import '../styles/auth.css'

const initialValues = {
  email: '',
  password: '',
  confirmPassword: '',
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

  if (!values.confirmPassword) {
    errors.confirmPassword = 'Confirm your password'
  } else if (values.confirmPassword !== values.password) {
    errors.confirmPassword = 'Passwords do not match'
  }

  return errors
}

function SignUpPage() {
  const { isAuthenticated, signup, user } = useAuth()
  const navigate = useNavigate()
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState('')
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
    setFormError('')
    setSubmitted(true)

    if (Object.keys(nextErrors).length > 0) {
      return
    }

    const result = await signup({ email: values.email, password: values.password })

    if (!result.ok) {
      setFormError(result.message)
      return
    }

    navigate(homePathForRole(result.user.role))
  }

  return (
    <main className="login-page">
      <AuthCard>
        <h1 className="auth-title auth-title-signup">Sign up</h1>

        <form className="auth-form" noValidate onSubmit={handleSubmit}>
          <AuthInput
            autoComplete="email"
            error={submitted ? errors.email : ''}
            helperText="enter email address"
            id="signup-email"
            label="Email"
            name="email"
            onChange={handleChange}
            type="email"
            value={values.email}
          />

          <AuthInput
            autoComplete="new-password"
            error={submitted ? errors.password : ''}
            helperText="create password"
            id="signup-password"
            label="Password"
            name="password"
            onChange={handleChange}
            type="password"
            value={values.password}
          />

          <AuthInput
            autoComplete="new-password"
            error={submitted ? errors.confirmPassword : ''}
            helperText="repeat password"
            id="confirm-password"
            label="Confirm password"
            name="confirmPassword"
            onChange={handleChange}
            type="password"
            value={values.confirmPassword}
          />

          <AuthButton type="submit">Sign up</AuthButton>
          {formError ? (
            <p className="auth-message auth-message-error auth-form-error" role="alert">
              {formError}
            </p>
          ) : null}
        </form>

        {isAuthenticated ? (
          <p className="auth-status" role="status">
            Account created for {user.email}
          </p>
        ) : (
          <Link className="auth-link" to="/login">
            Already have an account? Log in!
          </Link>
        )}
      </AuthCard>
    </main>
  )
}

export default SignUpPage
