import CloseIcon from '@mui/icons-material/Close'
import SearchIcon from '@mui/icons-material/Search'
import { useEffect, useState } from 'react'

function SearchBar({ onClear, onSearch, value }) {
  const [inputValue, setInputValue] = useState(value)

  useEffect(() => {
    setInputValue(value)
  }, [value])

  function handleClear() {
    setInputValue('')
    onClear()
  }

  function handleSubmit(event) {
    event.preventDefault()
    onSearch(inputValue)
  }

  return (
    <form className="home-search" role="search" aria-label="Search products" onSubmit={handleSubmit}>
      <input
        aria-label="Search products"
        className="home-search-input"
        onChange={(event) => setInputValue(event.target.value)}
        placeholder="Search..."
        type="search"
        value={inputValue}
      />
      {inputValue ? (
        <button
          aria-label="Clear search"
          className="home-search-action"
          onClick={handleClear}
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
      <button
        aria-label="Search"
        className="home-search-submit"
        type="submit"
      >
        <SearchIcon className="home-search-icon" fontSize="inherit" aria-hidden="true" />
      </button>
    </form>
  )
}

export default SearchBar
