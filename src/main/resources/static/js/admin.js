/**
 * admin.js - Admin dashboard and management pages
 * DineSync Restaurant Reservation System
 */

// ============================================================
// ADMIN AUTH CHECK
// ============================================================

function requireAdmin() {
  const user = Session.get();
  if (!user || user.role !== 'ADMIN') {
    window.location.href = '/login.html';
    return null;
  }
  return user;
}

// Set admin user info in sidebar
function initAdminSidebar() {
  const user = Session.get();
  if (!user) return;

  const nameEl = document.getElementById('admin-user-name');
  const avatarEl = document.getElementById('admin-avatar');

  if (nameEl) nameEl.textContent = user.name;
  if (avatarEl) avatarEl.textContent = user.name[0].toUpperCase();
}

// Mobile sidebar toggle
function initAdminHamburger() {
  const hamburger = document.querySelector('.admin-hamburger');
  const sidebar = document.querySelector('.admin-sidebar');

  if (hamburger && sidebar) {
    hamburger.addEventListener('click', () => {
      sidebar.classList.toggle('open');
    });

    document.addEventListener('click', (e) => {
      if (!sidebar.contains(e.target) && !hamburger.contains(e.target)) {
        sidebar.classList.remove('open');
      }
    });
  }
}

// ============================================================
// DASHBOARD PAGE
// ============================================================

async function loadDashboard() {
  if (!requireAdmin()) return;

  // Load statistics
  try {
    const stats = await Api.get('/admin/dashboard');
    document.getElementById('stat-total-res').textContent = stats.totalReservations ?? 0;
    document.getElementById('stat-today-res').textContent = stats.todayReservations ?? 0;
    document.getElementById('stat-customers').textContent = stats.totalCustomers ?? 0;
    document.getElementById('stat-restaurants').textContent = stats.totalRestaurants ?? 0;

    renderRecentReservations(stats.recentReservations || []);
  } catch (err) {
    Toast.error('Failed to load dashboard data.');
  }

  // Load analytics
  try {
    const analytics = await Api.get('/admin/analytics');
    renderBookingChart(analytics.reservationsPerDay || []);
    renderTopRestaurants(analytics.mostBookedRestaurants || []);

    const avgEl = document.getElementById('avg-guests');
    if (avgEl) avgEl.textContent = analytics.averageGuests || 0;
  } catch (err) {
    console.error('Analytics error:', err);
  }
}

function renderRecentReservations(reservations) {
  const tbody = document.getElementById('recent-reservations-body');
  if (!tbody) return;

  if (!reservations.length) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted" style="padding:24px;">No recent reservations</td></tr>`;
    return;
  }

  tbody.innerHTML = reservations.map(r => `
    <tr>
      <td>#${r.reservationId}</td>
      <td class="td-name">${escapeHtml(r.userName || '—')}</td>
      <td>${escapeHtml(r.restaurantName || '—')}</td>
      <td>${formatDate(r.reservationDate)}</td>
      <td>${formatTime(r.startTime + '')}</td>
      <td>${statusBadge(r.status)}</td>
    </tr>
  `).join('');
}

function renderBookingChart(data) {
  const container = document.getElementById('booking-chart');
  if (!container || !data.length) return;

  const max = Math.max(...data.map(d => d.count), 1);

  container.innerHTML = data.map(d => `
    <div class="chart-bar-group">
      <div class="chart-bar-value">${d.count}</div>
      <div class="chart-bar" style="height:${Math.max((d.count / max) * 180, 4)}px;" title="${d.count} bookings"></div>
      <div class="chart-bar-label">${formatDateShort(d.reservation_date)}</div>
    </div>
  `).join('');
}

function renderTopRestaurants(data) {
  const el = document.getElementById('top-restaurants');
  if (!el) return;

  el.innerHTML = data.map((r, i) => `
    <div style="display:flex; align-items:center; justify-content:space-between; padding:10px 0; border-bottom:1px solid var(--border-light);">
      <div style="display:flex; align-items:center; gap:12px;">
        <span style="font-size:1.1rem;">${['🥇','🥈','🥉','4️⃣','5️⃣'][i] || (i+1)+'.'}</span>
        <span style="font-weight:600; font-size:0.9rem;">${escapeHtml(r.restaurant_name)}</span>
      </div>
      <span class="badge badge-info">${r.booking_count} bookings</span>
    </div>
  `).join('');
}

function formatDateShort(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-IN', { month: 'short', day: 'numeric' });
}

// ============================================================
// ADMIN RESTAURANTS PAGE
// ============================================================

let adminRestaurants = [];

async function loadAdminRestaurants() {
  if (!requireAdmin()) return;
  showLoading('restaurants-table-body', 'Loading restaurants...');

  try {
    adminRestaurants = await Api.get('/restaurants');
    renderAdminRestaurants(adminRestaurants);
  } catch (err) {
    Toast.error('Failed to load restaurants.');
  }
}

function renderAdminRestaurants(restaurants) {
  const tbody = document.getElementById('restaurants-table-body');
  if (!tbody) return;

  if (!restaurants.length) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted" style="padding:32px;">No restaurants found</td></tr>`;
    return;
  }

  tbody.innerHTML = restaurants.map(r => `
    <tr>
      <td>
        <div class="admin-restaurant-cell">
          ${r.imageUrl ? `<img src="${escapeHtml(r.imageUrl)}" class="admin-restaurant-img" onerror="this.style.display='none'">` : ''}
          <span class="td-name">${escapeHtml(r.name)}</span>
        </div>
      </td>
      <td>${escapeHtml(r.cuisine)}</td>
      <td>${escapeHtml(r.location)}</td>
      <td>${r.priceRange}</td>
      <td><span class="rating-badge">${r.rating}</span></td>
      <td>${formatTime(r.openingTime + '')} – ${formatTime(r.closingTime + '')}</td>
      <td>
        <div class="td-actions">
          <button class="btn btn-sm btn-outline" onclick="openEditRestaurant(${r.restaurantId})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="deleteRestaurant(${r.restaurantId})">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function openAddRestaurant() {
  clearRestaurantForm();
  document.getElementById('restaurant-modal-title').textContent = 'Add Restaurant';
  document.getElementById('restaurant-modal').classList.add('active');
}

function openEditRestaurant(id) {
  const r = adminRestaurants.find(x => x.restaurantId === id);
  if (!r) return;

  document.getElementById('restaurant-modal-title').textContent = 'Edit Restaurant';
  document.getElementById('r-id').value = r.restaurantId;
  document.getElementById('r-name').value = r.name;
  document.getElementById('r-description').value = r.description || '';
  document.getElementById('r-location').value = r.location;
  document.getElementById('r-cuisine').value = r.cuisine;
  document.getElementById('r-price').value = r.priceRange;
  document.getElementById('r-phone').value = r.phone || '';
  document.getElementById('r-opening').value = r.openingTime?.substring(0, 5) || '';
  document.getElementById('r-closing').value = r.closingTime?.substring(0, 5) || '';
  document.getElementById('r-image').value = r.imageUrl || '';

  document.getElementById('restaurant-modal').classList.add('active');
}

function clearRestaurantForm() {
  document.getElementById('r-id').value = '';
  ['r-name','r-description','r-location','r-cuisine','r-phone','r-opening','r-closing','r-image'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  document.getElementById('r-price').value = '$$';
}

function closeRestaurantModal() {
  document.getElementById('restaurant-modal').classList.remove('active');
}

async function saveRestaurant() {
  const id = document.getElementById('r-id').value;
  const body = {
    name: document.getElementById('r-name').value.trim(),
    description: document.getElementById('r-description').value.trim(),
    location: document.getElementById('r-location').value.trim(),
    cuisine: document.getElementById('r-cuisine').value.trim(),
    priceRange: document.getElementById('r-price').value,
    phone: document.getElementById('r-phone').value.trim(),
    openingTime: document.getElementById('r-opening').value,
    closingTime: document.getElementById('r-closing').value,
    imageUrl: document.getElementById('r-image').value.trim()
  };

  if (!body.name || !body.location || !body.cuisine) {
    Toast.error('Name, location, and cuisine are required.');
    return;
  }

  try {
    const endpoint = id ? `/admin/restaurants/${id}` : '/admin/restaurants';
    const result = id ? await Api.put(endpoint, body) : await Api.post(endpoint, body);

    if (result.success) {
      Toast.success(id ? 'Restaurant updated!' : 'Restaurant added!');
      closeRestaurantModal();
      loadAdminRestaurants();
    } else {
      Toast.error(result.message || 'Failed to save restaurant.');
    }
  } catch (err) {
    Toast.error('Connection error.');
  }
}

async function deleteRestaurant(id) {
  if (!confirm('Delete this restaurant? This will also delete all tables, menus, and reservations.')) return;

  try {
    const result = await Api.delete(`/admin/restaurants/${id}`);
    if (result.success) {
      Toast.success('Restaurant deleted.');
      loadAdminRestaurants();
    } else {
      Toast.error(result.message || 'Failed to delete restaurant.');
    }
  } catch (err) {
    Toast.error('Connection error.');
  }
}

// ============================================================
// ADMIN TABLES PAGE
// ============================================================

let adminTables = [];
let selectedRestaurantForTables = null;

async function loadAdminTables() {
  if (!requireAdmin()) return;

  // Load restaurants for the dropdown
  try {
    const restaurants = await Api.get('/restaurants');
    const select = document.getElementById('table-restaurant-select');
    if (select) {
      select.innerHTML = '<option value="">Select a Restaurant</option>' +
        restaurants.map(r => `<option value="${r.restaurantId}">${escapeHtml(r.name)}</option>`).join('');

      select.addEventListener('change', () => {
        selectedRestaurantForTables = select.value;
        if (selectedRestaurantForTables) loadTablesByRestaurant(selectedRestaurantForTables);
      });
    }
  } catch (err) {
    Toast.error('Failed to load restaurants.');
  }
}

async function loadTablesByRestaurant(restaurantId) {
  showLoading('tables-table-body', 'Loading tables...');
  try {
    adminTables = await Api.get('/admin/tables', { restaurantId });
    renderAdminTables(adminTables);
  } catch (err) {
    Toast.error('Failed to load tables.');
  }
}

function renderAdminTables(tables) {
  const tbody = document.getElementById('tables-table-body');
  if (!tbody) return;

  if (!tables.length) {
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted" style="padding:32px;">No tables found for this restaurant</td></tr>`;
    return;
  }

  tbody.innerHTML = tables.map(t => `
    <tr>
      <td class="td-name">${escapeHtml(t.tableNumber)}</td>
      <td>${t.capacity}</td>
      <td>${escapeHtml(t.location)}</td>
      <td>${formatDate(t.createdAt?.split('T')[0] || '')}</td>
      <td>
        <div class="td-actions">
          <button class="btn btn-sm btn-outline" onclick="openEditTable(${t.tableId})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="deleteTable(${t.tableId})">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function openAddTable() {
  if (!selectedRestaurantForTables) { Toast.error('Please select a restaurant first.'); return; }
  document.getElementById('table-modal-title').textContent = 'Add Table';
  document.getElementById('t-id').value = '';
  document.getElementById('t-number').value = '';
  document.getElementById('t-capacity').value = '';
  document.getElementById('t-location').value = 'Main Hall';
  document.getElementById('table-modal').classList.add('active');
}

function openEditTable(id) {
  const t = adminTables.find(x => x.tableId === id);
  if (!t) return;
  document.getElementById('table-modal-title').textContent = 'Edit Table';
  document.getElementById('t-id').value = t.tableId;
  document.getElementById('t-number').value = t.tableNumber;
  document.getElementById('t-capacity').value = t.capacity;
  document.getElementById('t-location').value = t.location;
  document.getElementById('table-modal').classList.add('active');
}

function closeTableModal() {
  document.getElementById('table-modal').classList.remove('active');
}

async function saveTable() {
  const id = document.getElementById('t-id').value;
  const body = {
    restaurantId: parseInt(selectedRestaurantForTables),
    tableNumber: document.getElementById('t-number').value.trim(),
    capacity: parseInt(document.getElementById('t-capacity').value),
    location: document.getElementById('t-location').value.trim()
  };

  if (!body.tableNumber || !body.capacity) { Toast.error('Table number and capacity are required.'); return; }

  try {
    const endpoint = id ? `/admin/tables/${id}` : '/admin/tables';
    const result = id ? await Api.put(endpoint, body) : await Api.post(endpoint, body);
    if (result.success) {
      Toast.success(id ? 'Table updated!' : 'Table added!');
      closeTableModal();
      loadTablesByRestaurant(selectedRestaurantForTables);
    } else {
      Toast.error(result.message || 'Failed to save table.');
    }
  } catch (err) { Toast.error('Connection error.'); }
}

async function deleteTable(id) {
  if (!confirm('Delete this table?')) return;
  try {
    const result = await Api.delete(`/admin/tables/${id}`);
    if (result.success) { Toast.success('Table deleted.'); loadTablesByRestaurant(selectedRestaurantForTables); }
    else Toast.error(result.message || 'Failed.');
  } catch (err) { Toast.error('Connection error.'); }
}

// ============================================================
// ADMIN MENU PAGE
// ============================================================

let adminMenuItems = [];
let selectedRestaurantForMenu = null;

async function loadAdminMenu() {
  if (!requireAdmin()) return;

  try {
    const restaurants = await Api.get('/restaurants');
    const select = document.getElementById('menu-restaurant-select');
    if (select) {
      select.innerHTML = '<option value="">Select a Restaurant</option>' +
        restaurants.map(r => `<option value="${r.restaurantId}">${escapeHtml(r.name)}</option>`).join('');

      select.addEventListener('change', () => {
        selectedRestaurantForMenu = select.value;
        if (selectedRestaurantForMenu) loadMenuByRestaurant(selectedRestaurantForMenu);
      });
    }
  } catch (err) { Toast.error('Failed to load restaurants.'); }
}

async function loadMenuByRestaurant(restaurantId) {
  showLoading('menu-table-body', 'Loading menu...');
  try {
    adminMenuItems = await Api.get(`/restaurants/${restaurantId}/menu`);
    renderAdminMenu(adminMenuItems);
  } catch (err) { Toast.error('Failed to load menu.'); }
}

function renderAdminMenu(items) {
  const tbody = document.getElementById('menu-table-body');
  if (!tbody) return;

  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted" style="padding:32px;">No menu items</td></tr>`;
    return;
  }

  tbody.innerHTML = items.map(item => `
    <tr>
      <td class="td-name">${escapeHtml(item.name)}</td>
      <td>${escapeHtml(item.category)}</td>
      <td>₹${parseFloat(item.price).toFixed(0)}</td>
      <td>${escapeHtml(item.description || '—')}</td>
      <td>${item.available ? '<span class="badge badge-success">Available</span>' : '<span class="badge badge-danger">Unavailable</span>'}</td>
      <td>
        <div class="td-actions">
          <button class="btn btn-sm btn-outline" onclick="openEditMenu(${item.menuId})">Edit</button>
          <button class="btn btn-sm btn-danger" onclick="deleteMenuItem(${item.menuId})">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function openAddMenu() {
  if (!selectedRestaurantForMenu) { Toast.error('Please select a restaurant first.'); return; }
  ['m-id','m-name','m-description','m-price','m-image'].forEach(id => { const el = document.getElementById(id); if(el) el.value=''; });
  document.getElementById('m-category').value = 'Main';
  document.getElementById('m-available').checked = true;
  document.getElementById('menu-modal-title').textContent = 'Add Menu Item';
  document.getElementById('menu-modal').classList.add('active');
}

function openEditMenu(id) {
  const item = adminMenuItems.find(x => x.menuId === id);
  if (!item) return;
  document.getElementById('menu-modal-title').textContent = 'Edit Menu Item';
  document.getElementById('m-id').value = item.menuId;
  document.getElementById('m-name').value = item.name;
  document.getElementById('m-description').value = item.description || '';
  document.getElementById('m-category').value = item.category;
  document.getElementById('m-price').value = item.price;
  document.getElementById('m-image').value = item.imageUrl || '';
  document.getElementById('m-available').checked = item.available;
  document.getElementById('menu-modal').classList.add('active');
}

function closeMenuModal() { document.getElementById('menu-modal').classList.remove('active'); }

async function saveMenuItem() {
  const id = document.getElementById('m-id').value;
  const body = {
    restaurantId: parseInt(selectedRestaurantForMenu),
    name: document.getElementById('m-name').value.trim(),
    description: document.getElementById('m-description').value.trim(),
    category: document.getElementById('m-category').value,
    price: document.getElementById('m-price').value,
    imageUrl: document.getElementById('m-image').value.trim(),
    available: document.getElementById('m-available').checked
  };

  if (!body.name || !body.price) { Toast.error('Name and price are required.'); return; }

  try {
    const endpoint = id ? `/admin/menu/${id}` : '/admin/menu';
    const result = id ? await Api.put(endpoint, body) : await Api.post(endpoint, body);
    if (result.success) {
      Toast.success(id ? 'Updated!' : 'Menu item added!');
      closeMenuModal();
      loadMenuByRestaurant(selectedRestaurantForMenu);
    } else { Toast.error(result.message || 'Failed.'); }
  } catch (err) { Toast.error('Connection error.'); }
}

async function deleteMenuItem(id) {
  if (!confirm('Delete this menu item?')) return;
  try {
    const result = await Api.delete(`/admin/menu/${id}`);
    if (result.success) { Toast.success('Deleted.'); loadMenuByRestaurant(selectedRestaurantForMenu); }
    else Toast.error(result.message || 'Failed.');
  } catch (err) { Toast.error('Connection error.'); }
}

// ============================================================
// ADMIN RESERVATIONS PAGE
// ============================================================

async function loadAdminReservations() {
  if (!requireAdmin()) return;
  showLoading('admin-res-tbody', 'Loading reservations...');

  const date = document.getElementById('filter-date')?.value || '';
  const status = document.getElementById('filter-status')?.value || '';

  try {
    const reservations = await Api.get('/admin/reservations', { date, status });
    renderAdminReservations(reservations);
  } catch (err) { Toast.error('Failed to load reservations.'); }
}

function renderAdminReservations(reservations) {
  const tbody = document.getElementById('admin-res-tbody');
  if (!tbody) return;

  if (!reservations.length) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted" style="padding:32px;">No reservations found</td></tr>`;
    return;
  }

  tbody.innerHTML = reservations.map(r => `
    <tr>
      <td>#${r.reservationId}</td>
      <td class="td-name">${escapeHtml(r.userName || '—')}</td>
      <td>${escapeHtml(r.restaurantName || '—')}</td>
      <td>${escapeHtml(r.tableNumber || '—')}</td>
      <td>${formatDate(r.reservationDate)}</td>
      <td>${formatTime(r.startTime + '')}</td>
      <td>${r.guests}</td>
      <td>${statusBadge(r.status)}</td>
      <td>
        ${r.status === 'CONFIRMED' ? `<button class="btn btn-sm btn-danger" onclick="adminCancelReservation(${r.reservationId})">Cancel</button>` : '—'}
      </td>
    </tr>
  `).join('');
}

async function adminCancelReservation(id) {
  if (!confirm('Cancel this reservation?')) return;
  try {
    const result = await Api.delete(`/admin/reservations/${id}`);
    if (result.success) { Toast.success('Reservation cancelled.'); loadAdminReservations(); }
    else Toast.error(result.message || 'Failed.');
  } catch (err) { Toast.error('Connection error.'); }
}

// ============================================================
// ADMIN CUSTOMERS PAGE
// ============================================================

async function loadAdminCustomers() {
  if (!requireAdmin()) return;
  showLoading('customers-tbody', 'Loading customers...');

  try {
    const customers = await Api.get('/admin/customers');
    renderAdminCustomers(customers);
  } catch (err) { Toast.error('Failed to load customers.'); }
}

function renderAdminCustomers(customers) {
  const tbody = document.getElementById('customers-tbody');
  if (!tbody) return;

  if (!customers.length) {
    tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted" style="padding:32px;">No customers found</td></tr>`;
    return;
  }

  tbody.innerHTML = customers.map(c => `
    <tr>
      <td>
        <div style="display:flex;align-items:center;gap:12px;">
          <div style="width:36px;height:36px;border-radius:50%;background:var(--primary-light);display:flex;align-items:center;justify-content:center;font-weight:700;color:var(--primary);">${c.name[0].toUpperCase()}</div>
          <span class="td-name">${escapeHtml(c.name)}</span>
        </div>
      </td>
      <td>${escapeHtml(c.email)}</td>
      <td>${escapeHtml(c.phone || '—')}</td>
      <td>${formatDate(c.createdAt?.split('T')[0] || '')}</td>
    </tr>
  `).join('');
}

// Customer search
function filterCustomers() {
  const query = document.getElementById('customer-search')?.value.toLowerCase() || '';
  const rows = document.querySelectorAll('#customers-tbody tr');
  rows.forEach(row => {
    const text = row.textContent.toLowerCase();
    row.style.display = text.includes(query) ? '' : 'none';
  });
}

// ============================================================
// HELPER
// ============================================================
function escapeHtml(str) {
  if (!str) return '';
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ============================================================
// INIT — detect current page and initialize
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  if (!requireAdmin()) return;
  initAdminSidebar();
  initAdminHamburger();

  const page = window.location.pathname;

  if (page.includes('dashboard')) loadDashboard();
  else if (page.includes('/admin/restaurants')) loadAdminRestaurants();
  else if (page.includes('/admin/tables'))      loadAdminTables();
  else if (page.includes('/admin/menu'))        loadAdminMenu();
  else if (page.includes('/admin/reservations')) {
    loadAdminReservations();
    document.getElementById('filter-date')?.addEventListener('change', loadAdminReservations);
    document.getElementById('filter-status')?.addEventListener('change', loadAdminReservations);
  }
  else if (page.includes('/admin/customers'))   {
    loadAdminCustomers();
    document.getElementById('customer-search')?.addEventListener('input', filterCustomers);
  }
});
