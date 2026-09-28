/**
 * restaurant.js - Single restaurant details page
 * DineSync Restaurant Reservation System
 */

let currentRestaurantId = null;

// ============================================================
// LOAD RESTAURANT DETAILS
// ============================================================

async function loadRestaurantDetails(restaurantId) {
  try {
    const data = await Api.get(`/restaurants/${restaurantId}`);

    if (!data.success) {
      document.getElementById('restaurant-main').innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">🍽️</div>
          <h3>Restaurant Not Found</h3>
          <p>The restaurant you're looking for doesn't exist or has been removed.</p>
          <a href="/restaurants.html" class="btn btn-outline">Browse Restaurants</a>
        </div>
      `;
      return;
    }

    renderRestaurant(data.restaurant, data.reviews);
    loadMenu(restaurantId);
  } catch (err) {
    document.getElementById('restaurant-main').innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h3>Failed to Load</h3>
        <p>Connection error. Please try again.</p>
        <button class="btn btn-primary" onclick="loadRestaurantDetails(${restaurantId})">Retry</button>
      </div>
    `;
  }
}

// ============================================================
// RENDER RESTAURANT
// ============================================================

function renderRestaurant(r, reviews) {
  document.title = `${r.name} — DineSync`;

  const imageEl = document.getElementById('restaurant-hero-img');
  if (imageEl) {
    imageEl.src = r.imageUrl || 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=1200&auto=format&fit=crop';
    imageEl.alt = r.name;
  }

  const setEl = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
  const setHtml = (id, val) => { const el = document.getElementById(id); if (el) el.innerHTML = val; };

  setEl('restaurant-name', r.name);
  setEl('restaurant-cuisine', r.cuisine);
  setEl('restaurant-location', '📍 ' + r.location);
  setEl('restaurant-phone', '📞 ' + (r.phone || 'Not available'));
  setEl('restaurant-price', formatPriceRange(r.priceRange));
  setEl('restaurant-hours', `🕐 ${formatTime(r.openingTime)} – ${formatTime(r.closingTime)}`);
  setEl('restaurant-description', r.description || '');
  setHtml('restaurant-rating', `<span class="rating-badge">${r.rating || 0}</span> ${renderStars(r.rating)}`);

  // Reserve button link
  const reserveBtn = document.getElementById('reserve-btn');
  if (reserveBtn) {
    reserveBtn.href = `/reservation.html?restaurantId=${r.restaurantId}`;
  }

  // Reviews
  renderReviews(reviews);
}

// ============================================================
// LOAD MENU
// ============================================================

async function loadMenu(restaurantId) {
  const menuContainer = document.getElementById('menu-container');
  if (!menuContainer) return;

  menuContainer.innerHTML = '<div class="spinner-overlay"><div class="spinner"></div></div>';

  try {
    const menuItems = await Api.get(`/restaurants/${restaurantId}/menu`);

    if (!menuItems.length) {
      showEmpty('menu-container', '📋', 'No Menu Available', 'This restaurant has not added menu items yet.');
      return;
    }

    // Group by category
    const categories = {};
    menuItems.forEach(item => {
      if (!categories[item.category]) categories[item.category] = [];
      categories[item.category].push(item);
    });

    let html = '';
    const catOrder = ['Starter', 'Main', 'Dessert', 'Drink'];
    const allCats = [...new Set([...catOrder, ...Object.keys(categories)])];

    allCats.forEach(cat => {
      if (!categories[cat]) return;
      html += `
        <div class="menu-category">
          <h3 class="menu-category-title">${cat}s</h3>
          <div class="menu-items-grid">
            ${categories[cat].map(renderMenuItem).join('')}
          </div>
        </div>
      `;
    });

    menuContainer.innerHTML = html;
  } catch (err) {
    menuContainer.innerHTML = `<p class="text-muted text-center">Failed to load menu.</p>`;
  }
}

function renderMenuItem(item) {
  const availableBadge = item.available === false
    ? `<span class="badge badge-danger" style="font-size:0.7rem;">Unavailable</span>`
    : '';

  return `
    <div class="menu-item-card ${item.available === false ? 'unavailable' : ''}">
      <div class="menu-item-info">
        <div style="display:flex; align-items:flex-start; gap:8px; margin-bottom:4px;">
          <div class="menu-item-name">${escapeHtml(item.name)}</div>
          ${availableBadge}
        </div>
        ${item.description ? `<p class="menu-item-desc">${escapeHtml(item.description)}</p>` : ''}
      </div>
      <div class="menu-item-price">₹${parseFloat(item.price).toFixed(0)}</div>
    </div>
  `;
}

// ============================================================
// RENDER REVIEWS
// ============================================================

function renderReviews(reviews) {
  const container = document.getElementById('reviews-container');
  if (!container) return;

  if (!reviews || !reviews.length) {
    showEmpty('reviews-container', '⭐', 'No Reviews Yet', 'Be the first to review this restaurant!');
    return;
  }

  container.innerHTML = reviews.map(r => `
    <div class="review-card">
      <div class="review-header">
        <div class="reviewer-avatar">${(r.userName || 'A')[0].toUpperCase()}</div>
        <div>
          <div class="reviewer-name">${escapeHtml(r.userName || 'Anonymous')}</div>
          <div class="review-date">${formatDate(r.createdAt?.split('T')[0] || '')}</div>
        </div>
        <div class="review-stars">${renderStars(r.rating)}</div>
      </div>
      ${r.comment ? `<p class="review-comment">${escapeHtml(r.comment)}</p>` : ''}
    </div>
  `).join('');
}

// ============================================================
// POST REVIEW
// ============================================================

function initReviewForm(restaurantId) {
  const form = document.getElementById('review-form');
  if (!form) return;

  const user = Session.get();

  if (!user) {
    const reviewSection = document.getElementById('review-form-section');
    if (reviewSection) {
      reviewSection.innerHTML = `
        <div class="auth-prompt">
          <p>Please <a href="/login.html?redirect=/restaurant.html?id=${restaurantId}" class="text-primary">log in</a> to leave a review.</p>
        </div>
      `;
    }
    return;
  }

  // Star rating interaction
  let selectedRating = 0;
  const stars = document.querySelectorAll('.review-star');
  stars.forEach(star => {
    star.addEventListener('click', () => {
      selectedRating = parseInt(star.dataset.value);
      stars.forEach((s, i) => {
        s.classList.toggle('filled', i < selectedRating);
      });
    });

    star.addEventListener('mouseenter', () => {
      const hovered = parseInt(star.dataset.value);
      stars.forEach((s, i) => s.classList.toggle('hovered', i < hovered));
    });

    star.addEventListener('mouseleave', () => {
      stars.forEach(s => s.classList.remove('hovered'));
    });
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!selectedRating) {
      Toast.error('Please select a star rating.');
      return;
    }

    const comment = document.getElementById('review-comment')?.value.trim();
    const submitBtn = form.querySelector('button[type="submit"]');

    submitBtn.disabled = true;
    submitBtn.textContent = 'Posting...';

    try {
      const result = await Api.post(`/restaurants/${restaurantId}/reviews`, {
        userId: user.userId,
        rating: selectedRating,
        comment
      });

      if (result.success) {
        Toast.success('Review posted! Thank you for your feedback.');
        form.reset();
        selectedRating = 0;
        stars.forEach(s => s.classList.remove('filled'));

        // Reload restaurant details to refresh the rating and review list
        await loadRestaurantDetails(currentRestaurantId);
      } else {
        Toast.error(result.message || 'Failed to post review.');
      }
    } catch (err) {
      Toast.error('Connection error. Please try again.');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Post Review';
    }
  });
}

// ============================================================
// HELPER
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
document.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(window.location.search);
  const restaurantId = params.get('id');

  if (!restaurantId) {
    window.location.href = '/restaurants.html';
    return;
  }

  currentRestaurantId = parseInt(restaurantId);
  loadRestaurantDetails(currentRestaurantId);
  initReviewForm(currentRestaurantId);
});
