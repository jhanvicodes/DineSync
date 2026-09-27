/**
 * main.js - Shared utilities used across all pages
 * DineSync Restaurant Reservation System
 */

// ============================================================
// CONSTANTS
// ============================================================
const API_BASE = '/api';

// ============================================================
// SESSION MANAGEMENT
// We store user info in localStorage (simple beginner-friendly approach)
// ============================================================

const Session = {
  /** Save user data after login/signup */
  set(user) {
    localStorage.setItem('dinesync_user', JSON.stringify(user));
  },

  /** Get current logged-in user */
  get() {
    const data = localStorage.getItem('dinesync_user');
    return data ? JSON.parse(data) : null;
  },

  /** Check if user is logged in */
  isLoggedIn() {
    return this.get() !== null;
  },

  /** Check if user is an admin */
  isAdmin() {
    const user = this.get();
    return user && user.role === 'ADMIN';
  },

  /** Log out */
  clear() {
    localStorage.removeItem('dinesync_user');
  }
};

// ============================================================
// TOAST NOTIFICATIONS
// ============================================================

const Toast = {
  container: null,

  /** Initialize toast container */
  init() {
    if (!this.container) {
      this.container = document.createElement('div');
      this.container.className = 'toast-container';
      document.body.appendChild(this.container);
    }
  },

  /** Show a toast notification */
  show(message, type = 'info', duration = 3500) {
    this.init();

    const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <span class="toast-icon">${icons[type] || icons.info}</span>
      <span class="toast-message">${message}</span>
    `;

    this.container.appendChild(toast);

    // Auto-remove after duration
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(40px)';
      toast.style.transition = '0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  },

  success(msg, duration) { this.show(msg, 'success', duration); },
  error(msg, duration)   { this.show(msg, 'error', duration); },
  info(msg, duration)    { this.show(msg, 'info', duration); },
};

// ============================================================
// NAVBAR — Update nav links based on login state
// ============================================================
function updateNavbar() {
  const user = Session.get();

  // Find nav action elements if they exist
  const loginLink = document.getElementById('nav-login');
  const signupLink = document.getElementById('nav-signup');
  const logoutBtn = document.getElementById('nav-logout');
  const myResLink = document.getElementById('nav-my-reservations');
  const profileLink = document.getElementById('nav-profile');
  const adminLink = document.getElementById('nav-admin');
  const userNameEl = document.getElementById('nav-username');

  if (user) {
    // Logged in
    if (loginLink) loginLink.style.display = 'none';
    if (signupLink) signupLink.style.display = 'none';
    if (logoutBtn) logoutBtn.style.display = 'inline-flex';
    if (myResLink) myResLink.style.display = 'inline-flex';
    if (profileLink) profileLink.style.display = 'inline-flex';
    if (userNameEl) userNameEl.textContent = user.name.split(' ')[0];

    if (adminLink) {
      adminLink.style.display = user.role === 'ADMIN' ? 'inline-flex' : 'none';
    }
  } else {
    // Not logged in
    if (loginLink) loginLink.style.display = 'inline-flex';
    if (signupLink) signupLink.style.display = 'inline-flex';
    if (logoutBtn) logoutBtn.style.display = 'none';
    if (myResLink) myResLink.style.display = 'none';
    if (profileLink) profileLink.style.display = 'none';
    if (adminLink) adminLink.style.display = 'none';
  }
}

// Logout handler
function logout() {
  Session.clear();
  Toast.info('Logged out successfully');
  setTimeout(() => window.location.href = '/', 1000);
}

// ============================================================
// MOBILE HAMBURGER MENU
// ============================================================
function initHamburger() {
  const hamburger = document.querySelector('.hamburger');
  const navLinks = document.querySelector('.nav-links');

  if (hamburger && navLinks) {
    hamburger.addEventListener('click', () => {
      navLinks.classList.toggle('open');
    });

    // Close nav when a link is clicked
    navLinks.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', () => navLinks.classList.remove('open'));
    });
  }
}

// ============================================================
// API HELPER — Centralized fetch with error handling
// ============================================================
const Api = {
  /**
   * Make a GET request to the backend API
   * @param {string} endpoint - e.g. '/restaurants'
   * @param {Object} params - Query parameters object
   * @returns {Promise<any>} - Parsed JSON response
   */
  async get(endpoint, params = {}) {
    const url = new URL(API_BASE + endpoint, window.location.origin);
    Object.entries(params).forEach(([key, val]) => {
      if (val !== null && val !== undefined && val !== '') {
        url.searchParams.set(key, val);
      }
    });

    const headers = {};
    const user = Session.get();
    if (user) headers['X-User-Id'] = String(user.userId);

    const response = await fetch(url.toString(), { headers });
    if (!response.ok) {
      throw new Error(`Request failed: ${response.status}`);
    }
    return response.json();
  },

  /**
   * Make a POST request
   */
  async post(endpoint, body) {
    const headers = { 'Content-Type': 'application/json' };
    const user = Session.get();
    if (user) headers['X-User-Id'] = String(user.userId);

    const response = await fetch(API_BASE + endpoint, {
      method: 'POST',
      headers,
      body: JSON.stringify(body)
    });
    return response.json();
  },

  /**
   * Make a PUT request
   */
  async put(endpoint, body) {
    const headers = { 'Content-Type': 'application/json' };
    const user = Session.get();
    if (user) headers['X-User-Id'] = String(user.userId);

    const response = await fetch(API_BASE + endpoint, {
      method: 'PUT',
      headers,
      body: JSON.stringify(body)
    });
    return response.json();
  },

  /**
   * Make a DELETE request
   */
  async delete(endpoint, params = {}) {
    const url = new URL(API_BASE + endpoint, window.location.origin);
    Object.entries(params).forEach(([k, v]) => url.searchParams.set(k, v));
    const headers = {};
    const user = Session.get();
    if (user) headers['X-User-Id'] = String(user.userId);
    const response = await fetch(url.toString(), { method: 'DELETE', headers });
    return response.json();
  }
};

// ============================================================
// UTILITY FUNCTIONS
// ============================================================

/** Format a date string "2026-09-28" → "28 Sep 2026" */
function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr + 'T00:00:00');
  return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

/** Format time "19:30:00" → "7:30 PM" */
function formatTime(timeStr) {
  if (!timeStr) return '';
  const [hours, minutes] = timeStr.split(':');
  const h = parseInt(hours);
  const ampm = h >= 12 ? 'PM' : 'AM';
  const h12 = h % 12 || 12;
  return `${h12}:${minutes} ${ampm}`;
}

/** Get today's date in YYYY-MM-DD format */
function getTodayString() {
  return new Date().toISOString().split('T')[0];
}

/** Render star rating HTML */
function renderStars(rating) {
  const stars = Math.round(rating);
  return Array.from({ length: 5 }, (_, i) =>
    `<span class="star ${i < stars ? 'filled' : ''}">★</span>`
  ).join('');
}

/** Render a badge for reservation status */
function statusBadge(status) {
  const map = {
    'CONFIRMED':  ['badge-success', '✓ Confirmed'],
    'CANCELLED':  ['badge-danger',  '✕ Cancelled'],
    'COMPLETED':  ['badge-info',    '✔ Completed']
  };
  const [cls, label] = map[status] || ['badge-warning', status];
  return `<span class="badge ${cls}">${label}</span>`;
}

/** Show loading spinner in an element */
function showLoading(containerId, message = 'Loading...') {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `
      <div class="spinner-overlay">
        <div>
          <div class="spinner" style="margin: 0 auto 16px;"></div>
          <p class="text-muted text-center" style="font-size:0.88rem;">${message}</p>
        </div>
      </div>
    `;
  }
}

/** Show empty state in an element */
function showEmpty(containerId, icon, title, message, action = '') {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">${icon}</div>
        <h3>${title}</h3>
        <p>${message}</p>
        ${action}
      </div>
    `;
  }
}

// ============================================================
// INIT ON PAGE LOAD
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  updateNavbar();
  initHamburger();

  // Hook up logout button
  const logoutBtn = document.getElementById('nav-logout');
  if (logoutBtn) logoutBtn.addEventListener('click', logout);
});
