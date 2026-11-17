// ==============================================================
// Library Management System - Common JavaScript Utilities
// ==============================================================

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
        <button style="background:none; border:none; cursor:pointer; font-size:1.1rem; color:inherit;" onclick="this.parentElement.remove()">×</button>
    `;

    container.appendChild(alert);

    setTimeout(() => {
        if (alert.parentElement) {
            alert.remove();
        }
    }, 4500);
}

/**
 * Check currently logged in authentication status
 */
async function checkAuthStatus() {
    try {
        const res = await fetch('/api/auth/status');
        const data = await res.json();
        return data.data; // { authenticated: boolean, username: string }
    } catch (e) {
        console.error('Failed to check auth status', e);
        return { authenticated: false, username: null };
    }
}

/**
 * Log out librarian
 */
async function logout() {
    try {
        const res = await fetch('/api/auth/logout', { method: 'POST' });
        const data = await res.json();
        showAlert('Logged out successfully', 'success');
        setTimeout(() => {
            window.location.href = '/index.html';
        }, 500);
    } catch (e) {
        showAlert('Logout failed: ' + e.message, 'error');
    }
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
