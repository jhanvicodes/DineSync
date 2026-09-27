/**
 * restaurants.js - Restaurant listing page logic
 * DineSync Restaurant Reservation System
 */

// ============================================================
// STATE
// ============================================================
let allRestaurants = [];

// ============================================================
// LOAD RESTAURANTS
// ============================================================

async function loadRestaurants(query = '', cuisine = '', location = '', priceRange = '') {
  showLoading('restaurants-container', 'Finding restaurants...');

  try {
    const restaurants = await Api.get('/restaurants', { query, cuisine, location, priceRange });
    allRestaurants = restaurants;
    renderRestaurants(restaurants);
  } catch (err) {
    document.getElementById('restaurants-container').innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h3>Connection Error</h3>
        <p>Could not load restaurants. Please check your connection and try again.</p>
        <button class="btn btn-primary" onclick="loadRestaurants()">Retry</button>
      </div>
    `;
  }
}

// ============================================================
// RENDER RESTAURANT CARDS
// ============================================================

function renderRestaurants(restaurants) {
  const container = document.getElementById('restaurants-container');
  const countEl = document.getElementById('result-count');

  if (countEl) {
    countEl.textContent = `${restaurants.length} restaurant${restaurants.length !== 1 ? 's' : ''} found`;
  }

  if (!restaurants.length) {
    showEmpty(
      'restaurants-container',
      '🍽️',
      'No restaurants found',
      'Try adjusting your search filters.',
      `<button class="btn btn-outline" onclick="resetFilters()">Clear Filters</button>`
    );
    return;
  }

  container.innerHTML = `<div class="restaurants-grid">${restaurants.map(renderRestaurantCard).join('')}</div>`;
}

function renderRestaurantCard(r) {
  const imageHtml = r.imageUrl
    ? `<img src="${r.imageUrl}" alt="${escapeHtml(r.name)}" class="restaurant-card-image" onerror="this.style.display='none'">`
    : `<div class="restaurant-card-image-placeholder">🍽️</div>`;

  return `
    <div class="restaurant-card" onclick="window.location.href='/restaurant.html?id=${r.restaurantId}'">
      ${imageHtml}
      <div class="restaurant-card-body">
        <div class="restaurant-card-header">
          <div>
            <div class="restaurant-card-name">${escapeHtml(r.name)}</div>
            <span class="cuisine-tag">${escapeHtml(r.cuisine)}</span>
          </div>
          <div class="rating-badge">${r.rating || '—'}</div>
        </div>
        <div class="restaurant-card-meta">
          <span class="meta-item">📍 ${escapeHtml(r.location)}</span>
          <span class="price-badge">${r.priceRange || '$$'}</span>
        </div>
        <p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:var(--space-md); display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
          ${escapeHtml(r.description || '')}
        </p>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span style="font-size:0.8rem; color:var(--text-muted);">🕐 ${formatTime(r.openingTime)} – ${formatTime(r.closingTime)}</span>
          <span class="btn btn-sm btn-outline">Reserve →</span>
        </div>
      </div>
    </div>
  `;
}

// ============================================================
// FILTER DROPDOWNS
// ============================================================

async function loadFilterOptions() {
  try {
    const [cuisines, locations] = await Promise.all([
      Api.get('/restaurants/cuisines'),
      Api.get('/restaurants/locations')
    ]);

    const cuisineSelect = document.getElementById('filter-cuisine');
    if (cuisineSelect) {
      cuisines.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c;
        opt.textContent = c;
        cuisineSelect.appendChild(opt);
      });
    }

    const locationSelect = document.getElementById('filter-location');
    if (locationSelect) {
      locations.forEach(l => {
        const opt = document.createElement('option');
        opt.value = l;
        opt.textContent = l;
        locationSelect.appendChild(opt);
      });
    }
  } catch (err) {
    console.error('Failed to load filter options:', err);
  }
}

// ============================================================
// APPLY FILTERS
// ============================================================

function applyFilters() {
  const query = document.getElementById('search-query')?.value.trim() || '';
  const cuisine = document.getElementById('filter-cuisine')?.value || '';
  const location = document.getElementById('filter-location')?.value || '';
  const priceRange = document.getElementById('filter-price')?.value || '';

  loadRestaurants(query, cuisine, location, priceRange);
}

function resetFilters() {
  const inputs = ['search-query', 'filter-cuisine', 'filter-location', 'filter-price'];
  inputs.forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  loadRestaurants();
}

// ============================================================
// ESCAPE HTML (prevent XSS)
// ============================================================
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// ============================================================
// INIT
// ============================================================
document.addEventListener('DOMContentLoaded', async () => {
  // Pre-fill search from URL params (if coming from homepage widget)
  const params = new URLSearchParams(window.location.search);
  const queryParam = params.get('query') || '';
  const cuisineParam = params.get('cuisine') || '';

  const searchInput = document.getElementById('search-query');
  if (searchInput && queryParam) searchInput.value = queryParam;

  const cuisineFilter = document.getElementById('filter-cuisine');
  if (cuisineFilter && cuisineParam) cuisineFilter.value = cuisineParam;

  // Load filter dropdown options
  await loadFilterOptions();

  // Initial load
  loadRestaurants(queryParam, cuisineParam);

  // Search button
  document.getElementById('search-btn')?.addEventListener('click', applyFilters);

  // Enter key in search
  document.getElementById('search-query')?.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') applyFilters();
  });

  // Filter changes (auto-apply)
  ['filter-cuisine', 'filter-location', 'filter-price'].forEach(id => {
    document.getElementById(id)?.addEventListener('change', applyFilters);
  });

  // Reset button
  document.getElementById('reset-btn')?.addEventListener('click', resetFilters);
});
