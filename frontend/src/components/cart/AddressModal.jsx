function AddressModal({ form, message, onChange, onClose, onSubmit, saving }) {
  function handleBackdropMouseDown(event) {
    if (event.target === event.currentTarget) {
      onClose()
    }
  }

  return (
    <div className="address-modal-backdrop" onMouseDown={handleBackdropMouseDown} role="presentation">
      <section aria-labelledby="address-modal-title" aria-modal="true" className="address-modal" role="dialog">
        <h2 id="address-modal-title">Address</h2>
        <form noValidate onSubmit={onSubmit}>
          <label className="address-modal-field">
            <span>Country</span>
            <input name="country" onChange={onChange} type="text" value={form.country} />
          </label>
          <label className="address-modal-field">
            <span>City</span>
            <input name="city" onChange={onChange} type="text" value={form.city} />
          </label>
          <label className="address-modal-field">
            <span>Address</span>
            <input name="address" onChange={onChange} type="text" value={form.address} />
          </label>
          <label className="address-modal-field">
            <span>Zip code</span>
            <input name="zipCode" onChange={onChange} type="text" value={form.zipCode} />
          </label>
          {message ? (
            <p className="address-modal-message" role="alert">
              {message}
            </p>
          ) : null}
          <div className="address-modal-actions">
            <button className="address-modal-secondary" onClick={onClose} type="button">
              Cancel
            </button>
            <button className="address-modal-submit" disabled={saving} type="submit">
              {saving ? 'Saving...' : 'Submit'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

export default AddressModal
