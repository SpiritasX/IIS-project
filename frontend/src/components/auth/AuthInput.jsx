function AuthInput({
  autoComplete,
  error,
  helperText,
  id,
  label,
  name,
  onChange,
  type = 'text',
  value,
}) {
  const helperId = `${id}-helper`
  const errorId = `${id}-error`

  return (
    <div className="auth-field">
      <label className="auth-label" htmlFor={id}>
        {label}
      </label>
      <input
        aria-describedby={error ? errorId : helperId}
        aria-invalid={Boolean(error)}
        autoComplete={autoComplete}
        className="auth-input"
        id={id}
        name={name}
        onChange={onChange}
        type={type}
        value={value}
      />
      {error ? (
        <p className="auth-message auth-message-error" id={errorId}>
          {error}
        </p>
      ) : (
        <p className="auth-message" id={helperId}>
          {helperText}
        </p>
      )}
    </div>
  )
}

export default AuthInput
