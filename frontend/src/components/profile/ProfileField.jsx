function ProfileField({ error, helperText = 'helper text', label, ...inputProps }) {
  return (
    <label className="profile-field">
      <span>{label}</span>
      <input {...inputProps} aria-invalid={Boolean(error)} className="profile-input" />
      <small className={error ? 'profile-helper profile-helper-error' : 'profile-helper'}>
        {error || helperText}
      </small>
    </label>
  )
}

export default ProfileField
