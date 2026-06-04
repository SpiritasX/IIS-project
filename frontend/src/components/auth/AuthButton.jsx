function AuthButton({ children, type = 'button' }) {
  return (
    <button className="auth-button" type={type}>
      {children}
    </button>
  )
}

export default AuthButton
