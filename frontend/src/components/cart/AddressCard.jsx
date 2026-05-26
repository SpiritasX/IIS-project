import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlineOutlined'
import PlaceOutlinedIcon from '@mui/icons-material/PlaceOutlined'

function AddressCard() {
  return (
    <section className="address-card" aria-label="Delivery address">
      <div className="address-row">
        <PlaceOutlinedIcon className="address-icon" fontSize="inherit" />
        <strong>Address Example, City Example, Country example</strong>
      </div>

      <div className="address-divider" />

      <button className="address-add-button" type="button">
        <AddCircleOutlineIcon className="address-icon" fontSize="inherit" />
        <span>Add new address</span>
      </button>
    </section>
  )
}

export default AddressCard
