import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AdminSidebar from '../components/admin/AdminSidebar'
import BotanistSidebar from '../components/botanist/BotanistSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/varieties.css'

const SOIL_TYPES = ['Crnica', 'Ilovača', 'Kisela zemlja', 'Malč', 'Pesak', 'Crvenica']

const EMPTY_FORM = {
  name: '',
  latinName: '',
  humidity: '',
  soil: '',
  instructions: '',
  categoryId: '',
  typeId: '',
  speciesId: '',
  storageSpaceTypeId: '',
}

function VarietiesPage({ basePath = '/botanist', api }) {
  const navigate = useNavigate()
  const { logout } = useAuth()
  const Sidebar = basePath === '/admin' ? AdminSidebar : BotanistSidebar

  const [varieties, setVarieties] = useState([])
  const [categories, setCategories] = useState([])
  const [storageSpaces, setStorageSpaces] = useState([])
  const [types, setTypes] = useState([])
  const [species, setSpecies] = useState([])

  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [formSuccess, setFormSuccess] = useState(false)

  const [searchTerm, setSearchTerm] = useState('')
  const [loadingList, setLoadingList] = useState(true)

  // Variety tree expand state (separate from taxonomy tree)
  const [vCats, setVCats] = useState(new Set())
  const [vTypes, setVTypes] = useState(new Set())
  const [vSpecies, setVSpecies] = useState(new Set())

  // Variety inline edit state
  const [editingVarietyId, setEditingVarietyId] = useState(null)
  const [varietyEditForm, setVarietyEditForm] = useState({})
  const [varietyEditError, setVarietyEditError] = useState('')
  const [varietyEditSubmitting, setVarietyEditSubmitting] = useState(false)

  // Taxonomy tree state
  const [categoryTree, setCategoryTree] = useState([])
  const [expandedCats, setExpandedCats] = useState(new Set())
  const [expandedTypes, setExpandedTypes] = useState(new Set())
  const [addingCat, setAddingCat] = useState(false)
  const [addingTypeToCat, setAddingTypeToCat] = useState(null)
  const [addingSpeciesToType, setAddingSpeciesToType] = useState(null)
  const [newCatName, setNewCatName] = useState('')
  const [newTypeName, setNewTypeName] = useState('')
  const [newSpeciesName, setNewSpeciesName] = useState('')
  const [editingNode, setEditingNode] = useState(null)
  const [editName, setEditName] = useState('')
  const [treeError, setTreeError] = useState('')
  const [treeSubmitting, setTreeSubmitting] = useState(false)

  async function refreshTree() {
    try {
      const res = await api.getTaxonomyTree()
      setCategoryTree(res.data)
    } catch {}
  }

  useEffect(() => {
    let ignore = false
    async function load() {
      try {
        const [varRes, catRes, locRes, treeRes] = await Promise.all([
          api.getVarieties(), api.getCategories(), api.getStorageSpaces(), api.getTaxonomyTree()
        ])
        if (!ignore) {
          setVarieties(varRes.data)
          setCategories(catRes.data)
          setStorageSpaces(locRes.data)
          setCategoryTree(treeRes.data)
        }
      } catch {
      } finally {
        if (!ignore) setLoadingList(false)
      }
    }
    load()
    return () => { ignore = true }
  }, [])

  useEffect(() => {
    if (!form.categoryId) {
      setTypes([])
      setSpecies([])
      setForm((prev) => ({ ...prev, typeId: '', speciesId: '' }))
      return
    }
    let ignore = false
    api.getTypesByCategory(form.categoryId).then((res) => {
      if (!ignore) {
        setTypes(res.data)
        setSpecies([])
        setForm((prev) => ({ ...prev, typeId: '', speciesId: '' }))
      }
    })
    return () => { ignore = true }
  }, [form.categoryId])

  useEffect(() => {
    if (!form.typeId) {
      setSpecies([])
      setForm((prev) => ({ ...prev, speciesId: '' }))
      return
    }
    let ignore = false
    api.getSpeciesByType(form.typeId).then((res) => {
      if (!ignore) {
        setSpecies(res.data)
        setForm((prev) => ({ ...prev, speciesId: '' }))
      }
    })
    return () => { ignore = true }
  }, [form.typeId])

  function handleField(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setFormError('')
    setFormSuccess(false)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.speciesId) { setFormError('Please select a category, type and species.'); return }
    if (!form.storageSpaceTypeId) { setFormError('Please select a storage space type.'); return }
    if (!form.name.trim()) { setFormError('Variety name is required.'); return }

    setSubmitting(true)
    setFormError('')
    try {
      const res = await api.addVariety({
        name: form.name.trim(),
        latinName: form.latinName.trim() || null,
        humidity: form.humidity ? Number(form.humidity) : null,
        soil: form.soil || null,
        instructions: form.instructions.trim() || null,
        speciesId: Number(form.speciesId),
        storageSpaceTypeId: Number(form.storageSpaceTypeId),
      })
      setVarieties((prev) => [...prev, res.data])
      setForm(EMPTY_FORM)
      setTypes([])
      setSpecies([])
      setFormSuccess(true)
      setTimeout(() => setFormSuccess(false), 4000)
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to add variety. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  // ─── Tree helpers ───────────────────────────────────────────────────────────

  function toggleCat(id) {
    setExpandedCats((prev) => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  function toggleType(id) {
    setExpandedTypes((prev) => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  function toggleVCat(id) {
    setVCats((prev) => { const n = new Set(prev); n.has(id) ? n.delete(id) : n.add(id); return n })
  }
  function toggleVType(id) {
    setVTypes((prev) => { const n = new Set(prev); n.has(id) ? n.delete(id) : n.add(id); return n })
  }
  function toggleVSpecies(id) {
    setVSpecies((prev) => { const n = new Set(prev); n.has(id) ? n.delete(id) : n.add(id); return n })
  }

  function startEditVariety(v) {
    setEditingVarietyId(v.id)
    setVarietyEditForm({
      name: v.name,
      latinName: v.latinName ?? '',
      humidity: v.humidity != null ? String(v.humidity) : '',
      soil: v.soil ?? '',
      instructions: v.instructions ?? '',
      storageSpaceTypeId: String(v.storageSpaceTypeId),
    })
    setVarietyEditError('')
  }

  function cancelEditVariety() {
    setEditingVarietyId(null)
    setVarietyEditForm({})
    setVarietyEditError('')
  }

  function handleVarietyEditField(e) {
    const { name, value } = e.target
    setVarietyEditForm((prev) => ({ ...prev, [name]: value }))
    setVarietyEditError('')
  }

  async function saveVariety() {
    if (!varietyEditForm.name?.trim()) { setVarietyEditError('Variety name is required.'); return }
    setVarietyEditSubmitting(true)
    setVarietyEditError('')
    try {
      const res = await api.updateVariety(editingVarietyId, {
        name: varietyEditForm.name.trim(),
        latinName: varietyEditForm.latinName.trim() || null,
        humidity: varietyEditForm.humidity ? Number(varietyEditForm.humidity) : null,
        soil: varietyEditForm.soil || null,
        instructions: varietyEditForm.instructions.trim() || null,
        storageSpaceTypeId: varietyEditForm.storageSpaceTypeId ? Number(varietyEditForm.storageSpaceTypeId) : null,
      })
      setVarieties((prev) => prev.map((v) => (v.id === editingVarietyId ? res.data : v)))
      cancelEditVariety()
    } catch (err) {
      setVarietyEditError(err.response?.data?.message ?? 'Failed to save changes.')
    } finally {
      setVarietyEditSubmitting(false)
    }
  }

  async function handleTreeAction(action) {
    setTreeSubmitting(true)
    setTreeError('')
    try {
      await action()
      await refreshTree()
      // Also refresh categories for the add-variety form dropdowns
      const catRes = await api.getCategories()
      setCategories(catRes.data)
    } catch (err) {
      setTreeError(err.response?.data?.message ?? 'Operation failed.')
    } finally {
      setTreeSubmitting(false)
    }
  }

  function startEdit(level, id, name) {
    setEditingNode({ level, id })
    setEditName(name)
    setTreeError('')
  }

  function cancelEdit() {
    setEditingNode(null)
    setEditName('')
  }

  async function saveEdit() {
    if (!editingNode) return
    const { level, id } = editingNode
    await handleTreeAction(async () => {
      if (level === 'cat') await api.renameCategory(id, editName)
      else if (level === 'type') await api.renameType(id, editName)
      else await api.renameSpecies(id, editName)
      cancelEdit()
    })
  }

  async function handleDeleteCat(id) {
    await handleTreeAction(() => api.deleteCategory(id))
  }

  async function handleDeleteType(id) {
    await handleTreeAction(() => api.deleteType(id))
  }

  async function handleDeleteSpecies(id) {
    await handleTreeAction(() => api.deleteSpecies(id))
  }

  async function handleAddCat() {
    if (!newCatName.trim()) return
    await handleTreeAction(async () => {
      await api.createCategory(newCatName.trim())
      setNewCatName('')
      setAddingCat(false)
    })
  }

  async function handleAddType(catId) {
    if (!newTypeName.trim()) return
    await handleTreeAction(async () => {
      await api.createType(catId, newTypeName.trim())
      setNewTypeName('')
      setAddingTypeToCat(null)
    })
  }

  async function handleAddSpecies(typeId) {
    if (!newSpeciesName.trim()) return
    await handleTreeAction(async () => {
      await api.createSpecies(typeId, newSpeciesName.trim())
      setNewSpeciesName('')
      setAddingSpeciesToType(null)
    })
  }

  // ─── Filtered varieties ──────────────────────────────────────────────────────

  const filtered = searchTerm
    ? varieties.filter(
        (v) =>
          v.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          v.latinName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          v.speciesName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          v.categoryName?.toLowerCase().includes(searchTerm.toLowerCase()),
      )
    : varieties

  const btnSm = { minWidth: 'unset', padding: '0 12px', height: 30, fontSize: 13 }
  const inputSm = { height: 30, fontSize: 13, padding: '0 8px' }

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button aria-label="Back to dashboard" className="home-logo-placeholder" onClick={() => navigate(basePath)} type="button">
          <span aria-hidden="true" />
        </button>
        <div className="home-header-controls">
          <SearchBar onChange={(e) => setSearchTerm(e.target.value)} onClear={() => setSearchTerm('')} value={searchTerm} />
        </div>
      </header>

      <PageTitle label="Varieties" onBack={handleLogout} />

      <section className="home-body">
        <Sidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          {/* ── Add variety form ── */}
          <section className="variety-form-card" aria-labelledby="add-variety-heading">
            <h2 className="variety-form-heading" id="add-variety-heading">Add new variety</h2>

            <form className="variety-form" noValidate onSubmit={handleSubmit}>
              {/* Taxonomy dropdowns */}
              <div className="variety-form-row variety-form-row-4">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="categoryId">Category</label>
                  <select className="variety-select" id="categoryId" name="categoryId" onChange={handleField} value={form.categoryId}>
                    <option value="">Select category</option>
                    {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="typeId">Subcategory</label>
                  <select className="variety-select" disabled={!form.categoryId} id="typeId" name="typeId" onChange={handleField} value={form.typeId}>
                    <option value="">Select subcategory</option>
                    {types.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
                  </select>
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="speciesId">Species</label>
                  <select className="variety-select" disabled={!form.typeId} id="speciesId" name="speciesId" onChange={handleField} value={form.speciesId}>
                    <option value="">Select species</option>
                    {species.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="storageSpaceTypeId">Storage space type *</label>
                  <select className="variety-select" id="storageSpaceTypeId" name="storageSpaceTypeId" onChange={handleField} value={form.storageSpaceTypeId}>
                    <option value="">Select storage space type</option>
                    {storageSpaces.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
                  </select>
                </div>
              </div>

              {/* Variety name + latin name */}
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="name">Variety name *</label>
                  <input className="variety-input" id="name" name="name" onChange={handleField} placeholder="e.g. English lavender" required type="text" value={form.name} />
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="latinName">Latin name</label>
                  <input className="variety-input" id="latinName" name="latinName" onChange={handleField} placeholder="e.g. Lavandula angustifolia" type="text" value={form.latinName} />
                </div>
              </div>

              {/* Humidity + Soil */}
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="humidity">Humidity (%)</label>
                  <input className="variety-input" id="humidity" max="100" min="0" name="humidity" onChange={handleField} placeholder="e.g. 60" step="1" type="number" value={form.humidity} />
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="soil">Soil type</label>
                  <select className="variety-select" id="soil" name="soil" onChange={handleField} value={form.soil}>
                    <option value="">Select soil type</option>
                    {SOIL_TYPES.map((s) => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
              </div>

              <div className="variety-form-row">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="instructions">Care instructions</label>
                  <textarea className="variety-textarea" id="instructions" name="instructions" onChange={handleField} placeholder="Describe care guidelines for this variety…" rows={3} value={form.instructions} />
                </div>
              </div>

              {formError && <p className="variety-form-error" role="alert">{formError}</p>}
              {formSuccess && <p className="variety-form-success" role="status">Variety added successfully.</p>}

              <div className="variety-form-actions">
                <button className="variety-submit-button" disabled={submitting} type="submit">
                  {submitting ? 'Saving…' : 'Add variety'}
                </button>
              </div>
            </form>
          </section>

          {/* ── Varieties list (hierarchical) ── */}
          <section aria-labelledby="varieties-list-heading" className="varieties-list-card">
            <h2 className="variety-form-heading" id="varieties-list-heading">
              Registered varieties <span className="varieties-count">({filtered.length})</span>
            </h2>
            {loadingList ? (
              <p className="varieties-empty">Loading varieties…</p>
            ) : filtered.length === 0 ? (
              <p className="varieties-empty">{searchTerm ? 'No varieties match your search.' : 'No varieties registered yet.'}</p>
            ) : (() => {
              // Build category → type → species tree from flat varieties
              const tree = {}
              filtered.forEach((v) => {
                if (!tree[v.categoryId]) tree[v.categoryId] = { name: v.categoryName, types: {} }
                const cat = tree[v.categoryId]
                if (!cat.types[v.typeId]) cat.types[v.typeId] = { name: v.typeName, species: {} }
                const tp = cat.types[v.typeId]
                if (!tp.species[v.speciesId]) tp.species[v.speciesId] = { name: v.speciesName, varieties: [] }
                tp.species[v.speciesId].varieties.push(v)
              })
              const expandAll = searchTerm.length > 0

              return Object.entries(tree).map(([catId, catData]) => {
                const catOpen = expandAll || vCats.has(Number(catId))
                return (
                  <div key={catId} style={{ marginBottom: 6 }}>
                    {/* Category row */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 0', borderBottom: '1px solid var(--color-border, #e5e7eb)', cursor: 'pointer' }} onClick={() => toggleVCat(Number(catId))}>
                      <span style={{ fontSize: 12, color: 'var(--color-text-secondary)', width: 16 }}>{catOpen ? '▼' : '▶'}</span>
                      <span style={{ fontWeight: 600, fontSize: 14, flex: 1 }}>{catData.name}</span>
                    </div>

                    {catOpen && (
                      <div style={{ paddingLeft: 24 }}>
                        {Object.entries(catData.types).map(([typeId, typeData]) => {
                          const typeOpen = expandAll || vTypes.has(Number(typeId))
                          return (
                            <div key={typeId} style={{ marginTop: 4 }}>
                              {/* Type row */}
                              <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 0', borderBottom: '1px solid var(--color-border, #f3f4f6)', cursor: 'pointer' }} onClick={() => toggleVType(Number(typeId))}>
                                <span style={{ fontSize: 11, color: 'var(--color-text-secondary)', width: 14 }}>{typeOpen ? '▼' : '▶'}</span>
                                <span style={{ fontSize: 13, flex: 1 }}>{typeData.name}</span>
                              </div>

                              {typeOpen && (
                                <div style={{ paddingLeft: 22 }}>
                                  {Object.entries(typeData.species).map(([speciesId, speciesData]) => {
                                    const spOpen = expandAll || vSpecies.has(Number(speciesId))
                                    return (
                                      <div key={speciesId} style={{ marginTop: 4 }}>
                                        {/* Species row */}
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '3px 0', cursor: 'pointer' }} onClick={() => toggleVSpecies(Number(speciesId))}>
                                          <span style={{ fontSize: 11, color: 'var(--color-text-secondary)', width: 14 }}>{spOpen ? '▼' : '▶'}</span>
                                          <span style={{ fontSize: 13, flex: 1 }}>
                                            {speciesData.name}
                                            <span className="varieties-count" style={{ marginLeft: 6 }}>({speciesData.varieties.length})</span>
                                          </span>
                                        </div>

                                        {spOpen && (
                                          <div style={{ paddingLeft: 22, marginTop: 4 }}>
                                            <table className="varieties-table">
                                              <thead>
                                                <tr>
                                                  <th>Variety</th>
                                                  <th>Latin name</th>
                                                  <th>Humidity</th>
                                                  <th>Soil</th>
                                                  <th>Storage type</th>
                                                  <th style={{ width: 60 }}></th>
                                                </tr>
                                              </thead>
                                              <tbody>
                                                {speciesData.varieties.map((v) => (
                                                  <React.Fragment key={v.id}>
                                                    <tr>
                                                      <td className="varieties-name">{v.name}</td>
                                                      <td style={{ fontStyle: 'italic', color: 'var(--color-text-secondary)' }}>{v.latinName ?? '—'}</td>
                                                      <td>{v.humidity != null ? `${v.humidity}%` : '—'}</td>
                                                      <td>{v.soil ?? '—'}</td>
                                                      <td>{v.storageSpaceTypeName}</td>
                                                      <td>
                                                        {editingVarietyId === v.id ? (
                                                          <button className="variety-submit-button" onClick={cancelEditVariety} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                                                        ) : (
                                                          <button className="variety-submit-button" onClick={() => startEditVariety(v)} type="button" style={btnSm}>Edit</button>
                                                        )}
                                                      </td>
                                                    </tr>
                                                    {editingVarietyId === v.id && (
                                                      <tr>
                                                        <td colSpan={6} style={{ padding: '12px 0' }}>
                                                          <div style={{ background: 'var(--color-surface, #f9fafb)', border: '1px solid var(--color-border, #e5e7eb)', borderRadius: 8, padding: '16px' }}>
                                                            <div className="variety-form-row variety-form-row-2" style={{ marginBottom: 10 }}>
                                                              <div className="variety-field">
                                                                <label className="variety-label" htmlFor={`ve-name-${v.id}`}>Variety name *</label>
                                                                <input className="variety-input" id={`ve-name-${v.id}`} name="name" onChange={handleVarietyEditField} type="text" value={varietyEditForm.name ?? ''} />
                                                              </div>
                                                              <div className="variety-field">
                                                                <label className="variety-label" htmlFor={`ve-latin-${v.id}`}>Latin name</label>
                                                                <input className="variety-input" id={`ve-latin-${v.id}`} name="latinName" onChange={handleVarietyEditField} type="text" value={varietyEditForm.latinName ?? ''} />
                                                              </div>
                                                            </div>
                                                            <div className="variety-form-row variety-form-row-2" style={{ marginBottom: 10 }}>
                                                              <div className="variety-field">
                                                                <label className="variety-label" htmlFor={`ve-hum-${v.id}`}>Humidity (%)</label>
                                                                <input className="variety-input" id={`ve-hum-${v.id}`} max="100" min="0" name="humidity" onChange={handleVarietyEditField} step="1" type="number" value={varietyEditForm.humidity ?? ''} />
                                                              </div>
                                                              <div className="variety-field">
                                                                <label className="variety-label" htmlFor={`ve-soil-${v.id}`}>Soil type</label>
                                                                <select className="variety-select" id={`ve-soil-${v.id}`} name="soil" onChange={handleVarietyEditField} value={varietyEditForm.soil ?? ''}>
                                                                  <option value="">None</option>
                                                                  {SOIL_TYPES.map((s) => <option key={s} value={s}>{s}</option>)}
                                                                </select>
                                                              </div>
                                                            </div>
                                                            <div className="variety-form-row" style={{ marginBottom: 10 }}>
                                                              <div className="variety-field">
                                                                <label className="variety-label" htmlFor={`ve-storage-${v.id}`}>Storage space type</label>
                                                                <select className="variety-select" id={`ve-storage-${v.id}`} name="storageSpaceTypeId" onChange={handleVarietyEditField} value={varietyEditForm.storageSpaceTypeId ?? ''}>
                                                                  <option value="">Select storage space type</option>
                                                                  {storageSpaces.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
                                                                </select>
                                                              </div>
                                                            </div>
                                                            <div className="variety-form-row" style={{ marginBottom: 10 }}>
                                                              <div className="variety-field">
                                                                <label className="variety-label" htmlFor={`ve-instr-${v.id}`}>Care instructions</label>
                                                                <textarea className="variety-textarea" id={`ve-instr-${v.id}`} name="instructions" onChange={handleVarietyEditField} rows={3} value={varietyEditForm.instructions ?? ''} />
                                                              </div>
                                                            </div>
                                                            {varietyEditError && <p className="variety-form-error" role="alert" style={{ marginBottom: 8 }}>{varietyEditError}</p>}
                                                            <div style={{ display: 'flex', gap: 8 }}>
                                                              <button className="variety-submit-button" disabled={varietyEditSubmitting} onClick={saveVariety} type="button" style={btnSm}>
                                                                {varietyEditSubmitting ? 'Saving…' : 'Save'}
                                                              </button>
                                                              <button className="variety-submit-button" onClick={cancelEditVariety} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                                                            </div>
                                                          </div>
                                                        </td>
                                                      </tr>
                                                    )}
                                                  </React.Fragment>
                                                ))}
                                              </tbody>
                                            </table>
                                          </div>
                                        )}
                                      </div>
                                    )
                                  })}
                                </div>
                              )}
                            </div>
                          )
                        })}
                      </div>
                    )}
                  </div>
                )
              })
            })()}
          </section>

          {/* ── Taxonomy tree ── */}
          <section className="varieties-list-card" aria-labelledby="taxonomy-heading">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h2 className="variety-form-heading" id="taxonomy-heading" style={{ margin: 0 }}>Taxonomy</h2>
              {!addingCat && (
                <button className="variety-submit-button" onClick={() => setAddingCat(true)} type="button" style={btnSm}>
                  + Add category
                </button>
              )}
            </div>

            {treeError && <p className="variety-form-error" role="alert" style={{ marginBottom: 12 }}>{treeError}</p>}

            {/* Add category inline form */}
            {addingCat && (
              <div style={{ display: 'flex', gap: 8, marginBottom: 12, alignItems: 'center' }}>
                <input
                  autoFocus
                  className="variety-input"
                  onChange={(e) => setNewCatName(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter') handleAddCat(); if (e.key === 'Escape') { setAddingCat(false); setNewCatName('') } }}
                  placeholder="Category name"
                  style={{ ...inputSm, flex: 1 }}
                  type="text"
                  value={newCatName}
                />
                <button className="variety-submit-button" disabled={treeSubmitting} onClick={handleAddCat} type="button" style={btnSm}>Add</button>
                <button className="variety-submit-button" onClick={() => { setAddingCat(false); setNewCatName('') }} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
              </div>
            )}

            {categoryTree.length === 0 && !addingCat && (
              <p className="varieties-empty">No categories yet.</p>
            )}

            {categoryTree.map((cat) => (
              <div key={cat.id} style={{ marginBottom: 8 }}>
                {/* Category row */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 0', borderBottom: '1px solid var(--color-border, #e5e7eb)' }}>
                  <button
                    onClick={() => toggleCat(cat.id)}
                    type="button"
                    style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 13, color: 'var(--color-text-secondary)', width: 20 }}
                  >
                    {expandedCats.has(cat.id) ? '▼' : '▶'}
                  </button>

                  {editingNode?.level === 'cat' && editingNode?.id === cat.id ? (
                    <input
                      autoFocus
                      className="variety-input"
                      onChange={(e) => setEditName(e.target.value)}
                      onKeyDown={(e) => { if (e.key === 'Enter') saveEdit(); if (e.key === 'Escape') cancelEdit() }}
                      style={{ ...inputSm, flex: 1 }}
                      type="text"
                      value={editName}
                    />
                  ) : (
                    <span
                      onClick={() => toggleCat(cat.id)}
                      style={{ flex: 1, fontWeight: 600, cursor: 'pointer', fontSize: 14 }}
                    >
                      {cat.name}
                      <span className="varieties-count" style={{ marginLeft: 6 }}>({cat.types.length})</span>
                    </span>
                  )}

                  {editingNode?.level === 'cat' && editingNode?.id === cat.id ? (
                    <>
                      <button className="variety-submit-button" disabled={treeSubmitting} onClick={saveEdit} type="button" style={btnSm}>Save</button>
                      <button className="variety-submit-button" onClick={cancelEdit} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                    </>
                  ) : (
                    <>
                      <button className="variety-submit-button" onClick={() => startEdit('cat', cat.id, cat.name)} type="button" style={btnSm}>Edit</button>
                      <button className="variety-submit-button btn-danger" disabled={treeSubmitting} onClick={() => handleDeleteCat(cat.id)} type="button" style={btnSm}>Delete</button>
                    </>
                  )}
                </div>

                {/* Types (subcategories) */}
                {expandedCats.has(cat.id) && (
                  <div style={{ paddingLeft: 28 }}>
                    {cat.types.map((tp) => (
                      <div key={tp.id} style={{ marginTop: 4 }}>
                        {/* Type row */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 0', borderBottom: '1px solid var(--color-border, #f3f4f6)' }}>
                          <button
                            onClick={() => toggleType(tp.id)}
                            type="button"
                            style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 12, color: 'var(--color-text-secondary)', width: 18 }}
                          >
                            {expandedTypes.has(tp.id) ? '▼' : '▶'}
                          </button>

                          {editingNode?.level === 'type' && editingNode?.id === tp.id ? (
                            <input
                              autoFocus
                              className="variety-input"
                              onChange={(e) => setEditName(e.target.value)}
                              onKeyDown={(e) => { if (e.key === 'Enter') saveEdit(); if (e.key === 'Escape') cancelEdit() }}
                              style={{ ...inputSm, flex: 1 }}
                              type="text"
                              value={editName}
                            />
                          ) : (
                            <span
                              onClick={() => toggleType(tp.id)}
                              style={{ flex: 1, cursor: 'pointer', fontSize: 13 }}
                            >
                              {tp.name}
                              <span className="varieties-count" style={{ marginLeft: 6 }}>({tp.species.length})</span>
                            </span>
                          )}

                          {editingNode?.level === 'type' && editingNode?.id === tp.id ? (
                            <>
                              <button className="variety-submit-button" disabled={treeSubmitting} onClick={saveEdit} type="button" style={btnSm}>Save</button>
                              <button className="variety-submit-button" onClick={cancelEdit} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                            </>
                          ) : (
                            <>
                              <button className="variety-submit-button" onClick={() => { startEdit('type', tp.id, tp.name) }} type="button" style={btnSm}>Edit</button>
                              <button className="variety-submit-button btn-danger" disabled={treeSubmitting} onClick={() => handleDeleteType(tp.id)} type="button" style={btnSm}>Delete</button>
                              <button className="variety-submit-button" onClick={() => { setAddingSpeciesToType(tp.id); setNewSpeciesName(''); setExpandedTypes((p) => { const n = new Set(p); n.add(tp.id); return n }) }} type="button" style={btnSm}>+ Species</button>
                            </>
                          )}
                        </div>

                        {/* Species */}
                        {expandedTypes.has(tp.id) && (
                          <div style={{ paddingLeft: 26 }}>
                            {tp.species.map((sp) => (
                              <div key={sp.id} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '3px 0' }}>
                                <span style={{ width: 14, color: 'var(--color-text-secondary)', fontSize: 11 }}>—</span>

                                {editingNode?.level === 'species' && editingNode?.id === sp.id ? (
                                  <input
                                    autoFocus
                                    className="variety-input"
                                    onChange={(e) => setEditName(e.target.value)}
                                    onKeyDown={(e) => { if (e.key === 'Enter') saveEdit(); if (e.key === 'Escape') cancelEdit() }}
                                    style={{ ...inputSm, flex: 1 }}
                                    type="text"
                                    value={editName}
                                  />
                                ) : (
                                  <span style={{ flex: 1, fontSize: 13 }}>
                                    {sp.name}
                                    {sp.varietyCount > 0 && (
                                      <span className="varieties-count" style={{ marginLeft: 6 }}>({sp.varietyCount} varieties)</span>
                                    )}
                                  </span>
                                )}

                                {editingNode?.level === 'species' && editingNode?.id === sp.id ? (
                                  <>
                                    <button className="variety-submit-button" disabled={treeSubmitting} onClick={saveEdit} type="button" style={btnSm}>Save</button>
                                    <button className="variety-submit-button" onClick={cancelEdit} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                                  </>
                                ) : (
                                  <>
                                    <button className="variety-submit-button" onClick={() => startEdit('species', sp.id, sp.name)} type="button" style={btnSm}>Edit</button>
                                    <button className="variety-submit-button btn-danger" disabled={treeSubmitting} onClick={() => handleDeleteSpecies(sp.id)} type="button" style={btnSm}>Delete</button>
                                  </>
                                )}
                              </div>
                            ))}

                            {/* Add species inline */}
                            {addingSpeciesToType === tp.id ? (
                              <div style={{ display: 'flex', gap: 8, marginTop: 6, alignItems: 'center' }}>
                                <input
                                  autoFocus
                                  className="variety-input"
                                  onChange={(e) => setNewSpeciesName(e.target.value)}
                                  onKeyDown={(e) => { if (e.key === 'Enter') handleAddSpecies(tp.id); if (e.key === 'Escape') { setAddingSpeciesToType(null); setNewSpeciesName('') } }}
                                  placeholder="Species name"
                                  style={{ ...inputSm, flex: 1 }}
                                  type="text"
                                  value={newSpeciesName}
                                />
                                <button className="variety-submit-button" disabled={treeSubmitting} onClick={() => handleAddSpecies(tp.id)} type="button" style={btnSm}>Add</button>
                                <button className="variety-submit-button" onClick={() => { setAddingSpeciesToType(null); setNewSpeciesName('') }} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                              </div>
                            ) : null}
                          </div>
                        )}
                      </div>
                    ))}

                    {/* Add type inline */}
                    {addingTypeToCat === cat.id ? (
                      <div style={{ display: 'flex', gap: 8, marginTop: 8, alignItems: 'center' }}>
                        <input
                          autoFocus
                          className="variety-input"
                          onChange={(e) => setNewTypeName(e.target.value)}
                          onKeyDown={(e) => { if (e.key === 'Enter') handleAddType(cat.id); if (e.key === 'Escape') { setAddingTypeToCat(null); setNewTypeName('') } }}
                          placeholder="Subcategory name"
                          style={{ ...inputSm, flex: 1 }}
                          type="text"
                          value={newTypeName}
                        />
                        <button className="variety-submit-button" disabled={treeSubmitting} onClick={() => handleAddType(cat.id)} type="button" style={btnSm}>Add</button>
                        <button className="variety-submit-button" onClick={() => { setAddingTypeToCat(null); setNewTypeName('') }} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                      </div>
                    ) : (
                      <button
                        className="variety-submit-button"
                        onClick={() => { setAddingTypeToCat(cat.id); setNewTypeName('') }}
                        type="button"
                        style={{ ...btnSm, marginTop: 8 }}
                      >
                        + Add subcategory
                      </button>
                    )}
                  </div>
                )}
              </div>
            ))}
          </section>
        </div>
      </section>
    </main>
  )
}

export default VarietiesPage
