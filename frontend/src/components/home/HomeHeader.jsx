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
  onToggleFilter,
  searchTerm,
  sort,
  sortLabel,
  sortOptions,
}) {
  return (
    <header className="home-header">
      <div className="home-logo-placeholder" aria-hidden="true">
        <span />
      </div>

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
              onCategoryChange={onCategoryChange}
              onReset={onResetFilters}
              onSortChange={onSortChange}
              sort={sort}
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
