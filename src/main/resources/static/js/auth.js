/**
 * auth.js - Handles login and signup form logic
 * DineSync Restaurant Reservation System
 */

// ============================================================
// LOGIN PAGE
// ============================================================

/** Initialize the login form */
function initLoginPage() {
  const form = document.getElementById('login-form');
  if (!form) return;

  if (!Session.hasValidUser()) {
    Session.clear();
  }

  // If already logged in, redirect home
  if (Session.isLoggedIn()) {
    window.location.href = '/';
    return;
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const alert = document.getElementById('login-alert');
    const submitBtn = form.querySelector('button[type="submit"]');

    if (!email || !password) {
      showAlert(alert, 'Please enter your email and password.', 'error');
      return;
    }

    // Disable button while loading
    submitBtn.disabled = true;
    submitBtn.textContent = 'Logging in...';

    try {
      const result = await Api.post('/auth/login', { email, password });

      if (result.success) {
        // Save user session
        Session.set({
          userId: result.userId,
          name: result.name,
          email: result.email,
          phone: result.phone,
          role: result.role
        });

        Toast.success(`Welcome back, ${result.name.split(' ')[0]}! 🎉`);

        // Redirect based on role
        const redirectTo = new URLSearchParams(window.location.search).get('redirect') || '/';
        setTimeout(() => {
          window.location.href = result.role === 'ADMIN' ? '/admin/dashboard.html' : redirectTo;
        }, 800);
      } else {
        showAlert(alert, result.message || 'Invalid email or password.', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Log In';
      }
    } catch (err) {
      showAlert(alert, 'Connection error. Please try again.', 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'Log In';
    }
  });

  // Password visibility toggle
  initPasswordToggle('password', 'toggle-password');
}

// ============================================================
// SIGNUP PAGE
// ============================================================

/** Initialize the signup form */
function initSignupPage() {
  const form = document.getElementById('signup-form');
  if (!form) return;

  if (Session.isLoggedIn()) {
    window.location.href = '/';
    return;
  }

  const passwordInput = document.getElementById('password');

  // Password strength indicator
  if (passwordInput) {
    passwordInput.addEventListener('input', () => {
      updatePasswordStrength(passwordInput.value);
    });
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const alert = document.getElementById('signup-alert');
    const submitBtn = form.querySelector('button[type="submit"]');

    // Validation
    if (!name || !email || !password) {
      showAlert(alert, 'Please fill in all required fields.', 'error');
      return;
    }

    if (password.length < 6) {
      showAlert(alert, 'Password must be at least 6 characters.', 'error');
      return;
    }

    if (password !== confirmPassword) {
      showAlert(alert, 'Passwords do not match.', 'error');
      return;
    }

    if (!isValidEmail(email)) {
      showAlert(alert, 'Please enter a valid email address.', 'error');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Creating account...';

    try {
      const result = await Api.post('/auth/signup', { name, email, phone, password });

      if (result.success) {
        // Auto-login after signup
        Session.set({
          userId: result.userId,
          name: result.name,
          email: result.email,
          role: result.role
        });

        Toast.success('Account created successfully! Welcome to DineSync 🎉');
        setTimeout(() => window.location.href = '/', 1000);
      } else {
        showAlert(alert, result.message || 'Could not create account.', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Account';
      }
    } catch (err) {
      showAlert(alert, 'Connection error. Please try again.', 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'Create Account';
    }
  });

  // Password toggles
  initPasswordToggle('password', 'toggle-password');
  initPasswordToggle('confirm-password', 'toggle-confirm-password');
}

// ============================================================
// PROFILE PAGE
// ============================================================

function initProfilePage() {
  const user = Session.get();
  if (!user) {
    window.location.href = '/login.html?redirect=/profile.html';
    return;
  }

  // Fill in profile form
  const nameEl = document.getElementById('profile-name');
  const emailEl = document.getElementById('profile-email');
  const phoneEl = document.getElementById('profile-phone');

  if (nameEl) nameEl.value = user.name;
  if (emailEl) emailEl.value = user.email;
  if (phoneEl) phoneEl.value = user.phone || '';

  const form = document.getElementById('profile-form');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const name = document.getElementById('profile-name').value.trim();
    const phone = document.getElementById('profile-phone').value.trim();
    const submitBtn = form.querySelector('button[type="submit"]');

    if (!name) {
      Toast.error('Name is required.');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    try {
      const result = await Api.put('/auth/profile', { userId: user.userId, name, phone });

      if (result.success) {
        // Update session with new name
        const updated = { ...user, name: result.name, phone: result.phone };
        Session.set(updated);
        Toast.success('Profile updated successfully!');
        updateNavbar();
      } else {
        Toast.error(result.message || 'Failed to update profile.');
      }
    } catch (err) {
      Toast.error('Connection error. Please try again.');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Save Changes';
    }
  });
}

// ============================================================
// HELPER FUNCTIONS
// ============================================================

/** Show an alert box */
function showAlert(alertEl, message, type) {
  if (!alertEl) return;
  alertEl.textContent = message;
  alertEl.className = `auth-alert visible ${type}`;
}

/** Show/hide password toggle */
function initPasswordToggle(inputId, toggleId) {
  const input = document.getElementById(inputId);
  const toggle = document.getElementById(toggleId);
  if (!input || !toggle) return;

  toggle.addEventListener('click', () => {
    const isHidden = input.type === 'password';
    input.type = isHidden ? 'text' : 'password';
    toggle.textContent = isHidden ? '🙈' : '👁️';
  });
}

/** Basic email validation */
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

/** Update password strength bars */
function updatePasswordStrength(password) {
  const bars = document.querySelectorAll('.strength-bar');
  if (!bars.length) return;

  let strength = 0;
  if (password.length >= 6) strength++;
  if (password.length >= 8) strength++;
  if (/[A-Z]/.test(password) && /[0-9]/.test(password)) strength++;

  const classes = ['', 'weak', 'fair', 'strong'];
  bars.forEach((bar, i) => {
    bar.className = 'strength-bar';
    if (i < strength) bar.classList.add(classes[strength]);
  });
}

// ============================================================
// PAGE INIT
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  // Detect which page we're on based on the form presence
  if (document.getElementById('login-form'))   initLoginPage();
  if (document.getElementById('signup-form'))  initSignupPage();
  if (document.getElementById('profile-form')) initProfilePage();
});
