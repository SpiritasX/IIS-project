const defaultCategoryOptions = ['All', 'Flowers', 'Herbs', 'Trees']
const defaultSortOptions = [
  { label: 'Price', value: 'price' },
  { label: 'Name', value: 'name' },
]
const defaultOrderOptions = [
  { label: 'Ascending', value: 'asc' },
  { label: 'Descending', value: 'desc' },
]

function FilterPanel({
  category,
  categoryLabel = 'Category',
  categoryOptions = defaultCategoryOptions,
  onCategoryChange,
  species,
  onSpeciesChange,
  variety,
  onVarietyChange,
  minPrice,
  onMinPriceChange,
  maxPrice,
  onMaxPriceChange,
  onReset,
  onSortChange,
  sort,
  onOrderChange,
  order,
  sortLabel = 'Sort by',
  sortOptions = defaultSortOptions,
  orderLabel = 'Order',
  orderOptions = defaultOrderOptions,
}) {
  return (
    <div className="filter-panel">
      <label className="filter-field">
        <span>{categoryLabel}</span>
        <select onChange={onCategoryChange} value={category}>
          {categoryOptions.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
      </label>

      <label className="filter-field">
        <span>Species</span>
        <input
          onChange={onSpeciesChange}
          placeholder="Any species"
          type="text"
          value={species}
        />
      </label>

      <label className="filter-field">
        <span>Variety</span>
        <input
          onChange={onVarietyChange}
          placeholder="Any variety"
          type="text"
          value={variety}
        />
      </label>

      <label className="filter-field">
        <span>Minimum price</span>
        <input
          min="0"
          onChange={onMinPriceChange}
          placeholder="0"
          type="number"
          value={minPrice}
        />
      </label>

      <label className="filter-field">
        <span>Maximum price</span>
        <input
          min="0"
          onChange={onMaxPriceChange}
          placeholder="0"
          type="number"
          value={maxPrice}
        />
      </label>

      <label className="filter-field">
        <span>{sortLabel}</span>
        <select onChange={onSortChange} value={sort}>
          {sortOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>

      <label className="filter-field">
        <span>{orderLabel}</span>
        <select onChange={onOrderChange} value={order}>
          {orderOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>

      <button className="filter-reset" onClick={onReset} type="button">
        Reset
      </button>
    </div>
  )
}

export default FilterPanel
