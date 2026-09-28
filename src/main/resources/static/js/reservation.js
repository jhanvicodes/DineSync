/**
 * reservation.js - Table availability search and booking logic
 * DineSync Restaurant Reservation System
 *
 * This is the most important JavaScript file — it implements
 * the core reservation flow:
 * 1. User enters date, time, and number of guests
 * 2. Frontend calls GET /api/restaurants/{id}/tables/available
 * 3. Available tables are displayed visually
 * 4. User selects a table
 * 5. User confirms → POST /api/reservations
 */

// State
let restaurantId = null;
let restaurantData = null;
let selectedTableId = null;
let selectedTableInfo = null;

// ============================================================
// LOAD RESTAURANT INFO
// ============================================================

async function loadRestaurantInfo(resId) {
  try {
    const data = await Api.get(`/restaurants/${resId}`);
    if (data.success) {
      restaurantData = data.restaurant;

      // Set page title and summary
      const nameEl = document.getElementById('res-restaurant-name');
      const locEl = document.getElementById('res-restaurant-location');
      if (nameEl) nameEl.textContent = restaurantData.name;
      if (locEl) locEl.textContent = '📍 ' + restaurantData.location;

      // Page header title
      const headerTitle = document.getElementById('page-restaurant-name');
      if (headerTitle) headerTitle.textContent = restaurantData.name;
    }
  } catch (err) {
    console.error('Failed to load restaurant info:', err);
  }
}

// ============================================================
// SEARCH AVAILABLE TABLES
// ============================================================

async function searchAvailableTables() {
  const date = document.getElementById('res-date').value;
  const time = document.getElementById('res-time').value;
  const guests = parseInt(document.getElementById('res-guests').value);

  // Validation
  if (!date) { Toast.error('Please select a date.'); return; }
  if (!time) { Toast.error('Please select a time.'); return; }
  if (!guests || guests < 1) { Toast.error('Please enter number of guests.'); return; }

  // Check date is not in past
  const today = getTodayString();
  if (date < today) {
    Toast.error('Please select a valid date. You cannot book for past dates.');
    return;
  }

  // Update summary sidebar
  updateSummary(date, time, guests);

  // Show loading state
  const tablesContainer = document.getElementById('tables-container');
  tablesContainer.innerHTML = `
    <div class="spinner-overlay">
      <div>
        <div class="spinner" style="margin:0 auto 12px;"></div>
        <p class="text-muted" style="font-size:0.85rem;">Checking availability...</p>
      </div>
    </div>
  `;

  // Show the tables section
  document.getElementById('tables-section').style.display = 'block';

  // Reset selected table
  selectedTableId = null;
  selectedTableInfo = null;
  updateConfirmButton();

  try {
    const availableTables = await Api.get(
      `/restaurants/${restaurantId}/tables/available`,
      { date, time, guests }
    );

    renderTables(availableTables, date, time, guests);
  } catch (err) {
    tablesContainer.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h3>Failed to Load Tables</h3>
        <p>Could not check table availability. Please try again.</p>
        <button class="btn btn-outline" onclick="searchAvailableTables()">Retry</button>
      </div>
    `;
  }
}

// ============================================================
// RENDER AVAILABLE TABLES
// ============================================================

function renderTables(tables, date, time, guests) {
  const container = document.getElementById('tables-container');

  if (!tables.length) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">🪑</div>
        <h3>No Tables Available</h3>
        <p>No tables are available for your selected date and time.<br>
           Try a different time or date.</p>
        <button class="btn btn-outline" onclick="document.getElementById('tables-section').style.display='none'">
          Change Search
        </button>
      </div>
    `;
    return;
  }

  const endTime = addHours(time, 2);

  container.innerHTML = `
    <div class="tables-info-bar">
      <span style="font-size:0.85rem; color:var(--text-muted);">
        Showing tables for <strong>${guests} guest${guests !== 1 ? 's' : ''}</strong>
        on <strong>${formatDate(date)}</strong> at <strong>${formatTime(time + ':00')}</strong>
        — <strong>${tables.length} available</strong>
      </span>
    </div>
    <div class="tables-grid" id="tables-grid">
      ${tables.map(table => renderTableCard(table)).join('')}
    </div>
  `;

  // Add click handlers
  document.querySelectorAll('.table-card[data-table-id]').forEach(card => {
    card.addEventListener('click', () => selectTable(card, tables));
  });
}

function renderTableCard(table) {
  const icons = { 'Window': '🪟', 'Patio': '🌿', 'Private': '🔒', 'Private Room': '🔒', 'Banquet': '🎉', 'Bar Side': '🍸', 'Main Dining': '🍽️', 'Main Hall': '🏛️', 'Indoor': '🏠', 'Garden View': '🌸', 'Private Booth': '🛋️' };
  const icon = icons[table.location] || '🪑';

  return `
    <div class="table-card" data-table-id="${table.tableId}" data-capacity="${table.capacity}" data-location="${escapeHtml(table.location)}" data-number="${escapeHtml(table.tableNumber)}">
      <span class="table-card-icon">${icon}</span>
      <div class="table-number">${escapeHtml(table.tableNumber)}</div>
      <div class="table-capacity">👥 Up to ${table.capacity} guests</div>
      <div class="table-location-label">📍 ${escapeHtml(table.location)}</div>
      <div style="margin-top:8px;">
        <span class="badge badge-success" style="font-size:0.68rem;">Available</span>
      </div>
    </div>
  `;
}

function selectTable(card, tables) {
  // Deselect all
  document.querySelectorAll('.table-card').forEach(c => c.classList.remove('selected'));

  // Select clicked
  card.classList.add('selected');

  selectedTableId = parseInt(card.dataset.tableId);
  selectedTableInfo = {
    id: selectedTableId,
    number: card.dataset.number,
    capacity: parseInt(card.dataset.capacity),
    location: card.dataset.location
  };

  // Update summary
  const summaryTable = document.getElementById('summary-table');
  if (summaryTable) {
    summaryTable.innerHTML = `
      <div class="summary-table-selected">
        <div class="table-num">${selectedTableInfo.number}</div>
        <div style="font-size:0.8rem; color:var(--text-muted);">Capacity ${selectedTableInfo.capacity} • ${selectedTableInfo.location}</div>
      </div>
    `;
  }

  updateConfirmButton();
  Toast.success(`Table ${selectedTableInfo.number} selected!`, 1500);
}

// ============================================================
// CONFIRM RESERVATION
// ============================================================

async function confirmReservation() {
  const user = Session.get();
  const userId = user ? (user.userId ?? user.user_id ?? user.id) : null;

  if (!user || !userId) {
    Session.clear();
    Toast.error('Please log in to make a reservation.');
    setTimeout(() => window.location.href = `/login.html?redirect=/reservation.html?restaurantId=${restaurantId}`, 1500);
    return;
  }

  if (!selectedTableId) {
    Toast.error('Please select a table first.');
    return;
  }

  const date = document.getElementById('res-date').value;
  const time = document.getElementById('res-time').value;
  const guests = parseInt(document.getElementById('res-guests').value);

  const confirmBtn = document.getElementById('confirm-btn');
  confirmBtn.disabled = true;
  confirmBtn.textContent = 'Confirming...';

  try {
    const result = await Api.post('/reservations', {
      userId: Number(userId),
      restaurantId: restaurantId,
      tableId: selectedTableId,
      date: date,
      time: time,
      guests: guests
    });

    if (result.success) {
      Toast.success('Reservation confirmed! Redirecting...');
      // Store confirmation data and redirect
      localStorage.setItem('dinesync_confirmation', JSON.stringify(result));
      setTimeout(() => {
        window.location.href = `/confirmation.html?id=${result.reservationId}`;
      }, 1000);
    } else {
      Toast.error(result.message || 'Failed to create reservation. Please try again.');
      // If table was just taken, refresh availability
      if (result.message && result.message.includes('just been reserved')) {
        selectedTableId = null;
        selectedTableInfo = null;
        await searchAvailableTables();
      }
      confirmBtn.disabled = false;
      confirmBtn.textContent = 'Confirm Reservation';
    }
  } catch (err) {
    const message = String(err?.message || '');
    if (message.includes('violates foreign key constraint') || message.includes('reservations_user_id_fkey') || message.includes('user_id')) {
      Session.clear();
      Toast.error('Your session is invalid. Please log in again.');
      setTimeout(() => {
        window.location.href = `/login.html?redirect=/reservation.html?restaurantId=${restaurantId}`;
      }, 1200);
      return;
    }

    Toast.error('Connection error. Please try again.');
    confirmBtn.disabled = false;
    confirmBtn.textContent = 'Confirm Reservation';
  }
}

// ============================================================
// UPDATE SUMMARY SIDEBAR
// ============================================================

function updateSummary(date, time, guests) {
  const setEl = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
  setEl('summary-date', formatDate(date));
  setEl('summary-time', formatTime(time + ':00') + ' – ' + formatTime(addHours(time, 2) + ':00'));
  setEl('summary-guests', guests + ' guest' + (guests !== 1 ? 's' : ''));

  const tableEl = document.getElementById('summary-table');
  if (tableEl) tableEl.innerHTML = `<p class="text-muted" style="font-size:0.85rem; font-style:italic;">Select a table below</p>`;
}

function updateConfirmButton() {
  const btn = document.getElementById('confirm-btn');
  if (!btn) return;
  const user = Session.get();

  if (!user) {
    btn.textContent = 'Log in to Reserve';
    btn.disabled = false;
  } else if (!selectedTableId) {
    btn.textContent = 'Select a Table';
    btn.disabled = true;
  } else {
    btn.textContent = `Confirm Reservation — Table ${selectedTableInfo?.number}`;
    btn.disabled = false;
  }
}

// ============================================================
// UTILITY
// ============================================================

/** Add hours to a "HH:MM" time string */
function addHours(timeStr, hours) {
  const [h, m] = timeStr.split(':').map(Number);
  const total = h + hours;
  return `${String(total % 24).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ============================================================
// INIT
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(window.location.search);
  restaurantId = params.get('restaurantId');

  if (!restaurantId) {
    window.location.href = '/restaurants.html';
    return;
  }

  // Set minimum date to today
  const dateInput = document.getElementById('res-date');
  if (dateInput) {
    dateInput.min = getTodayString();
    dateInput.value = getTodayString();
  }

  // Load restaurant info
  loadRestaurantInfo(restaurantId);

  // Hide tables section initially
  const tablesSection = document.getElementById('tables-section');
  if (tablesSection) tablesSection.style.display = 'none';

  // Search button
  document.getElementById('search-tables-btn')?.addEventListener('click', searchAvailableTables);

  // Confirm button
  const confirmBtn = document.getElementById('confirm-btn');
  if (confirmBtn) {
    confirmBtn.addEventListener('click', confirmReservation);
    updateConfirmButton();
  }
});
