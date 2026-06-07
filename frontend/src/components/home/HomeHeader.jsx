import { useNavigate } from 'react-router-dom'
import CartButton from './CartButton'
import FilterPanel from './FilterPanel'
import SearchBar from './SearchBar'

function HomeHeader({
  cartCount,
  category,
  categoryLabel,
  categoryOptions,
  filterOpen,
  onCategoryChange,
  onClearSearch,
  onResetFilters,
  onSearchChange,
  onSortChange,
  onOrderChange,
  onSpeciesChange,
  onVarietyChange,
  onMinPriceChange,
  onMaxPriceChange,
  onToggleFilter,
  searchTerm,
  sort,
  order,
  species,
  variety,
  minPrice,
  maxPrice,
  sortLabel,
  sortOptions,
}) {
  const navigate = useNavigate()

  return (
    <header className="home-header">
      <button
        aria-label="Go to home"
        className="home-logo-placeholder"
        onClick={() => navigate('/home')}
        type="button"
      >
        <span aria-hidden="true" />
      </button>

      <div className="home-header-controls">
        <SearchBar onChange={onSearchChange} onClear={onClearSearch} value={searchTerm} />

        <div className="home-filter-wrap">
          <button
            aria-expanded={filterOpen}
            className="home-filter-button"
            onClick={onToggleFilter}
            type="button"
          >
            Filter
          </button>
          {filterOpen ? (
            <FilterPanel
              category={category}
              categoryLabel={categoryLabel}
              categoryOptions={categoryOptions}
              species={species}
              onSpeciesChange={onSpeciesChange}
              variety={variety}
              onVarietyChange={onVarietyChange}
              minPrice={minPrice}
              onMinPriceChange={onMinPriceChange}
              maxPrice={maxPrice}
              onMaxPriceChange={onMaxPriceChange}
              onCategoryChange={onCategoryChange}
              onReset={onResetFilters}
              onSortChange={onSortChange}
              sort={sort}
              onOrderChange={onOrderChange}
              order={order}
              sortLabel={sortLabel}
              sortOptions={sortOptions}
            />
          ) : null}
        </div>
      </div>

      <CartButton count={cartCount} />
    </header>
  )
}

export default HomeHeader
