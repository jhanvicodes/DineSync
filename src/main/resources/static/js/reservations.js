/**
 * reservations.js - My Reservations page (view, modify, cancel)
 * DineSync Restaurant Reservation System
 */

let allUserReservations = [];

// ============================================================
// LOAD RESERVATIONS
// ============================================================

async function loadUserReservations() {
  const user = Session.get();
  const userId = user ? (user.userId ?? user.user_id ?? user.id) : null;

  if (!user || !userId) {
    Session.clear();
    window.location.href = '/login.html?redirect=/my-reservations.html';
    return;
  }

  showLoading('reservations-container', 'Loading your reservations...');

  try {
    const reservations = await Api.get(`/reservations/user/${Number(userId)}`);
    allUserReservations = reservations;
    renderActiveTab();
  } catch (err) {
    document.getElementById('reservations-container').innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h3>Failed to Load</h3>
        <p>Could not load your reservations. Please try again.</p>
        <button class="btn btn-primary" onclick="loadUserReservations()">Retry</button>
      </div>
    `;
  }
}

// ============================================================
// RENDER TABS
// ============================================================

let currentTab = 'upcoming';

function renderActiveTab() {
  const today = getTodayString();

  let filtered;
  if (currentTab === 'upcoming') {
    filtered = allUserReservations.filter(r =>
      r.status === 'CONFIRMED' && r.reservationDate >= today
    );
  } else {
    filtered = allUserReservations.filter(r =>
      r.status === 'CANCELLED' || r.status === 'COMPLETED' || r.reservationDate < today
    );
  }

  renderReservations(filtered);
}

function renderReservations(reservations) {
  const container = document.getElementById('reservations-container');

  if (!reservations.length) {
    const emptyMessages = {
      upcoming: {
        icon: '📅',
        title: 'No Upcoming Reservations',
        message: 'You have no confirmed reservations. Browse restaurants to make a booking.',
        action: `<a href="/restaurants.html" class="btn btn-primary">Browse Restaurants</a>`
      },
      past: {
        icon: '📋',
        title: 'No Past Reservations',
        message: 'Your past and cancelled reservations will appear here.',
        action: ''
      }
    };

    const em = emptyMessages[currentTab] || emptyMessages.upcoming;
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">${em.icon}</div>
        <h3>${em.title}</h3>
        <p>${em.message}</p>
        ${em.action}
      </div>
    `;
    return;
  }

  container.innerHTML = reservations.map(renderReservationItem).join('');
}

function renderReservationItem(r) {
  const date = new Date(r.reservationDate + 'T00:00:00');
  const day = date.toLocaleDateString('en-IN', { day: 'numeric' });
  const month = date.toLocaleDateString('en-IN', { month: 'short' });

  const isModifiable = r.status === 'CONFIRMED' && r.reservationDate >= getTodayString();

  const actions = isModifiable ? `
    <div class="res-actions">
      <button class="btn btn-sm btn-outline" onclick="openModifyModal(${r.reservationId})">
        ✏️ Modify
      </button>
      <button class="btn btn-sm btn-danger" onclick="cancelReservation(${r.reservationId})">
        Cancel
      </button>
    </div>
  ` : `
    <div class="res-actions">
      ${statusBadge(r.status)}
    </div>
  `;

  return `
    <div class="reservation-item" id="res-item-${r.reservationId}">
      <div class="res-date-block">
        <div class="res-date-day">${day}</div>
        <div class="res-date-month">${month}</div>
      </div>
      <div class="res-info">
        <div class="res-restaurant">${escapeHtml(r.restaurantName || 'Restaurant')}</div>
        <div class="res-details-row">
          <span>🕐 ${formatTime(r.startTime + '')} – ${formatTime(r.endTime + '')}</span>
          <span>👥 ${r.guests} guest${r.guests !== 1 ? 's' : ''}</span>
          <span>🪑 Table ${escapeHtml(r.tableNumber || '—')}</span>
          ${r.tableLocation ? `<span>📍 ${escapeHtml(r.tableLocation)}</span>` : ''}
        </div>
        <div>
          <span style="font-size:0.78rem; color:var(--text-muted);">Booking #${r.reservationId}</span>
        </div>
      </div>
      ${actions}
    </div>
  `;
}

// ============================================================
// CANCEL RESERVATION
// ============================================================

async function cancelReservation(reservationId) {
  if (!confirm('Are you sure you want to cancel this reservation?')) return;

  const user = Session.get();
  try {
    const result = await Api.delete(`/reservations/${reservationId}`, {
      userId: user.userId,
      isAdmin: false
    });

    if (result.success) {
      Toast.success('Reservation cancelled successfully.');
      await loadUserReservations();
    } else {
      Toast.error(result.message || 'Failed to cancel reservation.');
    }
  } catch (err) {
    Toast.error('Connection error. Please try again.');
  }
}

// ============================================================
// MODIFY RESERVATION
// ============================================================

let modifyingReservationId = null;

function openModifyModal(reservationId) {
  modifyingReservationId = reservationId;
  const reservation = allUserReservations.find(r => r.reservationId === reservationId);
  if (!reservation) return;

  // Populate modal
  const dateInput = document.getElementById('modify-date');
  const timeInput = document.getElementById('modify-time');
  const guestsInput = document.getElementById('modify-guests');

  if (dateInput) { dateInput.value = reservation.reservationDate; dateInput.min = getTodayString(); }
  if (timeInput) timeInput.value = reservation.startTime.substring(0, 5);
  if (guestsInput) guestsInput.value = reservation.guests;

  // Show modal
  document.getElementById('modify-modal').classList.add('active');
}

function closeModifyModal() {
  document.getElementById('modify-modal').classList.remove('active');
  modifyingReservationId = null;
}

async function submitModify() {
  if (!modifyingReservationId) return;

  const user = Session.get();
  const date = document.getElementById('modify-date').value;
  const time = document.getElementById('modify-time').value;
  const guests = parseInt(document.getElementById('modify-guests').value);

  if (!date || !time || !guests) {
    Toast.error('Please fill in all fields.');
    return;
  }

  const reservation = allUserReservations.find(r => r.reservationId === modifyingReservationId);

  const submitBtn = document.getElementById('modify-submit-btn');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Saving...';

  try {
    const result = await Api.put(`/reservations/${modifyingReservationId}`, {
      userId: user.userId,
      date,
      time,
      guests,
      tableId: reservation.tableId  // Keep same table; could allow table change for a full redesign
    });

    if (result.success) {
      Toast.success('Reservation updated successfully!');
      closeModifyModal();
      await loadUserReservations();
    } else {
      Toast.error(result.message || 'Failed to update reservation.');
    }
  } catch (err) {
    Toast.error('Connection error. Please try again.');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Save Changes';
  }
}

// ============================================================
// HELPER
// ============================================================
function escapeHtml(str) {
  if (!str) return '';
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ============================================================
// INIT
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  // Tab switching
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentTab = btn.dataset.tab;
      renderActiveTab();
    });
  });

  // Modal close buttons
  document.getElementById('modify-modal-close')?.addEventListener('click', closeModifyModal);
  document.getElementById('modify-cancel-btn')?.addEventListener('click', closeModifyModal);
  document.getElementById('modify-submit-btn')?.addEventListener('click', submitModify);

  // Close modal on overlay click
  document.getElementById('modify-modal')?.addEventListener('click', (e) => {
    if (e.target === e.currentTarget) closeModifyModal();
  });

  loadUserReservations();
});
