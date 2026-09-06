import React, { useState, useEffect, useCallback } from 'react';
import { proteinService } from '../services/api';
import { ProteinCard } from '../components/ProteinCard';
import { Search, Filter, Sparkles, RefreshCw, Zap } from 'lucide-react';

export const ProteinExplorer = ({
  bookmarks,
  onBookmarkClick,
  onEditComment,
  onDeleteBookmark,
  openAuthModal,
  addToast
}) => {
  const [proteins, setProteins] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters State
  const [sourceFilter, setSourceFilter] = useState('');
  const [dietFilter, setDietFilter] = useState(''); // 'veg', 'vegan', 'non-veg'
  const [searchQuery, setSearchQuery] = useState('');
  const [maxCostGrams, setMaxCostGrams] = useState(5.0);

  const fetchProteins = useCallback(async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await proteinService.getProteins(params);
      setProteins(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch proteins from backend:', err);
      setError(err.message || 'Could not fetch protein data from external API.');
      setProteins([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProteins();
  }, [fetchProteins]);

  const handleApplyFilters = (e) => {
    if (e) e.preventDefault();
    const params = {};
    if (sourceFilter) params.source = sourceFilter;
    if (dietFilter === 'veg') params.vegetarian = 'T';
    if (dietFilter === 'vegan') params.vegen = 'T';
    if (dietFilter === 'non-veg') params.vegetarian = 'F';
    
    fetchProteins(params);
  };

  const handlePresetSelect = (presetType) => {
    if (presetType === 'veg-under-1-50') {
      setSearchQuery('');
      setDietFilter('veg');
      setMaxCostGrams(1.50);
      fetchProteins({ vegetarian: 'T' });
    } else if (presetType === 'vegan') {
      setSearchQuery('');
      setDietFilter('vegan');
      setMaxCostGrams(5.0);
      fetchProteins({ vegen: 'T' });
    } else if (presetType === 'whey') {
      setSearchQuery('Whey');
      setDietFilter('');
      setMaxCostGrams(5.0);
      fetchProteins({});
    } else {
      setSearchQuery('');
      setDietFilter('');
      setMaxCostGrams(5.0);
      fetchProteins({});
    }
  };

  // Client-side filtering for search bar and max cost per gram slider
  const filteredProteins = proteins.filter(item => {
    // Match search in source field (e.g. "Chicken Breast", "Whey Protein", "Ground Turkey")
    const sourceName = String(item.source).toLowerCase();
    const matchesSearch = !searchQuery || sourceName.includes(searchQuery.toLowerCase());

    // Match cost_grams slider ($0 - $5.00/g)
    const itemCostGrams = item.cost_grams !== undefined ? Number(item.cost_grams) : 0;
    const matchesPrice = !maxCostGrams || itemCostGrams <= maxCostGrams || itemCostGrams === 0;

    // Match dietary filters
    let matchesDiet = true;
    if (dietFilter === 'veg') {
      matchesDiet = item.vegetarian === 'T';
    } else if (dietFilter === 'vegan') {
      matchesDiet = item.vegen === 'T';
    } else if (dietFilter === 'non-veg') {
      matchesDiet = item.vegetarian === 'F';
    }

    return matchesSearch && matchesPrice && matchesDiet;
  });

  // Map bookmarked protein IDs
  const bookmarkedMap = new Map();
  if (Array.isArray(bookmarks)) {
    bookmarks.forEach(b => {
      bookmarkedMap.set(String(b.proteinId), b);
    });
  }

  return (
    <div>
      <section className="hero-section">
        <h1 className="hero-title">
          Protein <span className="hero-gradient">Nutritional Intelligence</span>
        </h1>
        <p className="hero-subtitle">
          Query protein costs, package pricing, protein density, and dietary compatibility (Vegetarian & Vegan).
        </p>

        <div className="presets-bar">
          <button
            className={`preset-chip ${dietFilter === 'veg' && maxCostGrams === 1.50 ? 'active' : ''}`}
            onClick={() => handlePresetSelect('veg-under-1-50')}
          >
            <Sparkles size={14} /> Vegetarian &lt; $1.50/g
          </button>

          <button
            className={`preset-chip ${dietFilter === 'vegan' ? 'active' : ''}`}
            onClick={() => handlePresetSelect('vegan')}
          >
            🌱 Vegan Only
          </button>

          <button
            className={`preset-chip ${searchQuery === 'Whey' ? 'active' : ''}`}
            onClick={() => handlePresetSelect('whey')}
          >
            ⚡ Whey Powder
          </button>

          <button
            className={`preset-chip ${!searchQuery && !dietFilter && maxCostGrams === 5.0 ? 'active' : ''}`}
            onClick={() => handlePresetSelect('all')}
          >
            Show All
          </button>
        </div>
      </section>

      {/* Filter controls */}
      <div className="glass-panel search-filter-card">
        <form onSubmit={handleApplyFilters} className="filter-grid">
          <div className="form-group search-group">
            <label>Search Protein Source / Item</label>
            <div style={{ position: 'relative' }}>
              <input
                type="text"
                className="form-input"
                style={{ width: '100%', paddingLeft: '2.5rem' }}
                placeholder="e.g. Chicken, Whey, Tofu, Milk, Beef..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <Search size={18} style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            </div>
          </div>

          <div className="form-group">
            <label>Dietary Compatibility</label>
            <select
              className="form-select"
              value={dietFilter}
              onChange={(e) => setDietFilter(e.target.value)}
            >
              <option value="">All Dietary Types</option>
              <option value="veg">Vegetarian (vegetarian = T)</option>
              <option value="vegan">Vegan (vegen = T)</option>
              <option value="non-veg">Non-Vegetarian</option>
            </select>
          </div>

          <div className="form-group">
            <label>Max Cost / Gram: ${maxCostGrams.toFixed(2)}</label>
            <input
              type="range"
              min="0.50"
              max="5.00"
              step="0.10"
              value={maxCostGrams}
              onChange={(e) => setMaxCostGrams(Number(e.target.value))}
              style={{ accentColor: 'var(--accent-primary)', cursor: 'pointer', height: '36px' }}
            />
          </div>

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button type="submit" className="btn-primary" style={{ flex: 1 }}>
              <Filter size={16} /> Filter
            </button>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => fetchProteins()}
              title="Reload Data"
            >
              <RefreshCw size={16} />
            </button>
          </div>
        </form>
      </div>

      {/* Results Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem' }}>
          Available Products ({filteredProteins.length})
        </h2>
      </div>

      {/* Grid view */}
      {loading ? (
        <div className="empty-state">
          <Zap size={36} className="animate-spin text-emerald-400" />
          <p>Fetching protein intelligence data from Gateway (:8080)...</p>
        </div>
      ) : error ? (
        <div className="glass-panel empty-state" style={{ borderColor: 'rgba(239, 68, 68, 0.4)' }}>
          <p style={{ color: 'var(--danger)', fontWeight: 600 }}>{error}</p>
          <p style={{ marginTop: '0.5rem', fontSize: '0.9rem' }}>
            The backend protein-service (:8083) cannot connect to external port 3232.
          </p>
          <button className="btn-secondary" style={{ marginTop: '1rem' }} onClick={() => fetchProteins()}>
            Retry Connection
          </button>
        </div>
      ) : filteredProteins.length === 0 ? (
        <div className="glass-panel empty-state">
          <Search size={40} />
          <h3>No matching protein items found</h3>
          <p>Try expanding the cost per gram slider or clearing filters.</p>
          <button className="btn-secondary" style={{ marginTop: '1rem' }} onClick={() => handlePresetSelect('all')}>
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="cards-grid">
          {filteredProteins.map((protein, idx) => {
            const pId = String(protein.id);
            const savedBookmark = bookmarkedMap.get(pId);

            return (
              <ProteinCard
                key={pId}
                index={idx}
                protein={protein}
                bookmark={savedBookmark}
                onBookmarkClick={onBookmarkClick}
                onEditComment={onEditComment}
                onDeleteBookmark={onDeleteBookmark}
                openAuthModal={openAuthModal}
              />
            );
          })}
        </div>
      )}
    </div>
  );
};
