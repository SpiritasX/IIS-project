function ProfilePanel({ children, message, onSubmit, status, title }) {
  return (
    <form className="profile-panel" noValidate onSubmit={onSubmit}>
      <h2>{title}</h2>
      <div className="profile-panel-fields">{children}</div>
      {message ? (
        <p className={status === 'error' ? 'profile-status profile-status-error' : 'profile-status'}>
          {message}
        </p>
      ) : null}
      <button className="profile-submit-button" type="submit">
        Save
      </button>
    </form>
  )
}

export default ProfilePanel
