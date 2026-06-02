import CloseIcon from '@mui/icons-material/Close'
import SearchIcon from '@mui/icons-material/Search'

function SearchBar({ onChange, onClear, value }) {
  return (
    <label className="home-search" aria-label="Search products">
      <input
        className="home-search-input"
        onChange={onChange}
        placeholder="Search..."
        type="search"
        value={value}
      />
      {value ? (
        <button
          aria-label="Clear search"
          className="home-search-action"
          onClick={onClear}
          type="button"
        >
          <CloseIcon fontSize="inherit" />
        </button>
      ) : (
        <span className="home-search-action home-search-action-muted" aria-hidden="true">
          <CloseIcon fontSize="inherit" />
        </span>
      )}
      <span className="home-search-divider" aria-hidden="true" />
      <SearchIcon className="home-search-icon" fontSize="inherit" aria-hidden="true" />
    </label>
  )
}

export default SearchBar
