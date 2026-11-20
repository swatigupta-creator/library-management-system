// ==============================================================
// Library Management System - Common JavaScript Utilities
// ==============================================================

// Base URL: If not served directly from port 8080 (e.g. VS Code Live Server on 5500, or file:///), point to Spring Boot backend at http://localhost:8080
const API_BASE = (window.location.port === '8080')
    ? ''
    : 'http://localhost:8080';

const AUTH_STORAGE_KEY = 'bbau_lms_auth';

function getStoredAuth() {
    try {
        const item = localStorage.getItem(AUTH_STORAGE_KEY);
        return item ? JSON.parse(item) : null;
    } catch (e) {
        return null;
    }
}

function setStoredAuth(auth) {
    try {
        if (auth && auth.authenticated) {
            localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
        } else {
            localStorage.removeItem(AUTH_STORAGE_KEY);
        }
    } catch (e) {}
}

function clearStoredAuth() {
    try {
        localStorage.removeItem(AUTH_STORAGE_KEY);
    } catch (e) {}
}

/**
 * Universal API Fetch wrapper that handles:
 * - Credentials (cookies)
 * - Persistent student headers (X-Student-Id, X-Student-Email) across server restarts
 * - Smooth 401 Unauthorized handling
 */
async function apiFetch(url, options = {}) {
    const opts = { ...options };
    opts.credentials = 'include';
    opts.headers = { ...(opts.headers || {}) };

    const storedAuth = getStoredAuth();
    if (storedAuth && storedAuth.role === 'STUDENT') {
        if (storedAuth.memberId) opts.headers['X-Student-Id'] = String(storedAuth.memberId);
        if (storedAuth.email) opts.headers['X-Student-Email'] = String(storedAuth.email);
    }

    const res = await fetch(url, opts);

    // If session expired or unauthorized
    if (res.status === 401) {
        const clone = res.clone();
        try {
            const body = await clone.json();
            if (body && body.message && body.message.toLowerCase().includes('login required')) {
                clearStoredAuth();
                updateNavbar({ authenticated: false, role: null });
                if (typeof openRoleModal === 'function') {
                    openRoleModal();
                }
            }
        } catch (ignored) {}
    }

    return res;
}

/**
 * Safely parse JSON from a fetch Response.
 * Detects if the server or file system returned an HTML error page (e.g. 404, file not found)
 * and produces a friendly error instead of "Unexpected token '<' ... is not valid JSON".
 */
async function parseJsonResponse(res) {
    const text = await res.text();
    try {
        return JSON.parse(text);
    } catch (err) {
        if (text && text.trim().startsWith('<')) {
            throw new Error('Please open this website through http://localhost:8080/ (Spring Boot server must be running).');
        }
        throw new Error('Invalid response from server: ' + err.message);
    }
}

/**
 * Show a floating alert toast message
 * @param {string} message 
 * @param {'success'|'error'|'info'} type 
 */
function showAlert(message, type = 'info') {
    let container = document.getElementById('alert-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'alert-container';
        container.className = 'alert-container';
        document.body.appendChild(container);
    }

    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = `
        <span>${escapeHtml(message)}</span>
        <button style="background:none; border:none; cursor:pointer; font-size:1.1rem; color:inherit; margin-left:10px;" onclick="this.parentElement.remove()">×</button>
    `;

    container.appendChild(alert);

    setTimeout(() => {
        if (alert.parentElement) {
            alert.remove();
        }
    }, 4500);
}

/**
 * Check currently logged in authentication status & role
 * @returns {Promise<{authenticated: boolean, role: 'ADMIN'|'STUDENT'|null, name: string, username: string, email: string, memberId: number}>}
 */
async function checkAuthStatus() {
    try {
        const res = await apiFetch(`${API_BASE}/api/auth/status`);
        const json = await parseJsonResponse(res);
        if (json.success && json.data && json.data.authenticated) {
            setStoredAuth(json.data);
            return json.data;
        }
        clearStoredAuth();
        return { authenticated: false, role: null };
    } catch (e) {
        console.warn('Failed to check auth status', e);
        const stored = getStoredAuth();
        return stored || { authenticated: false, role: null };
    }
}

/**
 * Updates navbar based on user session role
 */
function updateNavbar(auth) {
    const navAuthItem = document.getElementById('nav-auth-item');
    if (!navAuthItem) return;

    if (!auth || !auth.authenticated) {
        navAuthItem.innerHTML = `
            <button onclick="openRoleModal()" class="nav-btn btn btn-primary" style="padding: 0.45rem 1rem;">
                🔑 Login / Register
            </button>
        `;
        return;
    }

    if (auth.role === 'ADMIN') {
        navAuthItem.innerHTML = `
            <div style="display: flex; align-items: center; gap: 0.75rem;">
                <span style="font-size: 0.85rem; color: var(--text-muted);">
                    🛡️ <strong style="color: var(--primary);">Librarian (Admin)</strong>
                </span>
                <a href="librarian.html" class="btn btn-primary btn-sm">Admin Dashboard</a>
                <button onclick="logout()" class="btn btn-outline btn-sm">Logout</button>
            </div>
        `;
    } else if (auth.role === 'STUDENT') {
        navAuthItem.innerHTML = `
            <div style="display: flex; align-items: center; gap: 0.75rem;">
                <span style="font-size: 0.85rem; color: var(--text-muted);">
                    🎓 <strong style="color: var(--primary);">${escapeHtml(auth.name || 'Student')}</strong>
                </span>
                <button onclick="logout()" class="btn btn-outline btn-sm">Logout</button>
            </div>
        `;
    }
}

/**
 * Log out user (Admin or Student)
 */
async function logout() {
    try {
        await apiFetch(`${API_BASE}/api/auth/logout`, { method: 'POST' });
    } catch (e) {}
    clearStoredAuth();
    showAlert('Logged out successfully', 'success');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 400);
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/**
 * Format date string (YYYY-MM-DD) to friendly format
 */
function formatDate(dateStr) {
    if (!dateStr) return '-';
    try {
        const date = new Date(dateStr);
        return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
    } catch (e) {
        return dateStr;
    }
}
