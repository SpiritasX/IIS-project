import ArrowBackIcon from '@mui/icons-material/ArrowBack'

function PageTitle({ label, onBack, wide = false }) {
  return (
    <section className={wide ? 'page-title-row page-title-row-wide' : 'page-title-row'}>
      <button className="home-back-button" onClick={onBack} type="button" aria-label="Back">
        <ArrowBackIcon fontSize="inherit" />
      </button>
      <h1>{label}</h1>
    </section>
  )
}

export default PageTitle
