const defaultCategoryOptions = ['All', 'Flowers', 'Herbs', 'Trees']
const defaultSortOptions = [
  { label: 'Featured', value: 'featured' },
  { label: 'Low to high', value: 'low-high' },
  { label: 'High to low', value: 'high-low' },
]

function FilterPanel({
  category,
  categoryLabel = 'Category',
  categoryOptions = defaultCategoryOptions,
  onCategoryChange,
  onReset,
  onSortChange,
  sort,
  sortLabel = 'Price',
  sortOptions = defaultSortOptions,
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
        <span>{sortLabel}</span>
        <select onChange={onSortChange} value={sort}>
          {sortOptions.map((option) => (
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
