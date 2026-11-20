// ==============================================================
// Library Management System - Librarian Login JavaScript
// ==============================================================

document.addEventListener('DOMContentLoaded', async () => {
    // If already authenticated, redirect straight to librarian portal
    const auth = await checkAuthStatus();
    if (auth && auth.authenticated && auth.role === 'ADMIN') {
        window.location.href = 'librarian.html';
        return;
    }

    const loginForm = document.getElementById('login-form');
    if (loginForm) loginForm.addEventListener('submit', handleLogin);
});

async function handleLogin(e) {
    e.preventDefault();

    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const submitBtn = document.getElementById('submit-btn');
    const errorBox = document.getElementById('login-error');

    // Reset error states
    errorBox.style.display = 'none';

    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    if (!username) {
        usernameInput.focus();
        return;
    }

    if (!password) {
        passwordInput.focus();
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Logging in...';

    try {
        const res = await apiFetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const json = await parseJsonResponse(res);

        if (res.ok && json.success) {
            showAlert('Login successful! Redirecting to Librarian Portal...', 'success');
            setTimeout(() => {
                window.location.href = 'librarian.html';
            }, 500);
        } else {
            errorBox.textContent = json.message || 'Invalid username or password';
            errorBox.style.display = 'block';
            submitBtn.disabled = false;
            submitBtn.textContent = 'Login to Dashboard';
        }
    } catch (err) {
        console.error('Login error', err);
        errorBox.textContent = 'Could not connect to backend server: ' + err.message;
        errorBox.style.display = 'block';
        submitBtn.disabled = false;
        submitBtn.textContent = 'Login to Dashboard';
    }
}
