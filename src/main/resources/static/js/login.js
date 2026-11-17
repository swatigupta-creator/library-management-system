// ==============================================================
// Library Management System - Librarian Login JavaScript
// ==============================================================

document.addEventListener('DOMContentLoaded', async () => {
    // If already authenticated, redirect straight to librarian portal
    const auth = await checkAuthStatus();
    if (auth && auth.authenticated) {
        window.location.href = '/librarian.html';
        return;
    }

    const loginForm = document.getElementById('login-form');
    loginForm.addEventListener('submit', handleLogin);
});

async function handleLogin(e) {
    e.preventDefault();

    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const submitBtn = document.getElementById('submit-btn');
    const errorBox = document.getElementById('login-error');

    // Reset error states
    errorBox.style.display = 'none';
    usernameInput.classList.remove('is-invalid');
    passwordInput.classList.remove('is-invalid');

    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    if (!username) {
        usernameInput.classList.add('is-invalid');
        usernameInput.focus();
        return;
    }

    if (!password) {
        passwordInput.classList.add('is-invalid');
        passwordInput.focus();
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Logging in...';

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const json = await res.json();

        if (res.ok && json.success) {
            showAlert('Login successful! Redirecting...', 'success');
            setTimeout(() => {
                window.location.href = '/librarian.html';
            }, 600);
        } else {
            errorBox.textContent = json.message || 'Invalid username or password';
            errorBox.style.display = 'block';
            submitBtn.disabled = false;
            submitBtn.textContent = 'Login to Dashboard';
        }
    } catch (err) {
        console.error('Login error', err);
        errorBox.textContent = 'Could not connect to backend server. Make sure Spring Boot is running.';
        errorBox.style.display = 'block';
        submitBtn.disabled = false;
        submitBtn.textContent = 'Login to Dashboard';
    }
}
