// ==============================================================
// Library Management System - Books Catalog JavaScript
// Supports: Live Search, Filter, Role Gate, Student Borrow/Return,
// and Admin Add/Edit/Delete
// ==============================================================

let allBooks = [];
let currentAuth = null;
let studentLoans = [];

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Check user authentication
    currentAuth = await checkAuthStatus();
    updateNavbar(currentAuth);

    if (!currentAuth || !currentAuth.authenticated) {
        // Not logged in: Show the Role Selection Gateway immediately
        openRoleModal();
    } else if (currentAuth.role === 'STUDENT') {
        showStudentQuickBar(currentAuth);
        await loadStudentDrawerLoans();
    }

    // 2. Adjust Admin Controls visibility
    adjustAdminControls();

    // 3. Load Books from Database
    await loadBooks();

    // 4. Setup Search & Filter Listeners
    setupSearchAndFilters();

    // 5. Setup Gateway & Modal Form Listeners
    setupModalForms();

    // 6. Setup Backdrop click dismiss & ESC key
    setupModalDismissListeners();
});

// --------------------------------------------------------------
// Adjust Admin Controls (Add, Edit, Delete options)
// --------------------------------------------------------------
function adjustAdminControls() {
    const isAdmin = currentAuth && currentAuth.role === 'ADMIN';

    const adminActionsBar = document.getElementById('admin-actions-bar');
    const adminTableAction = document.getElementById('admin-table-action');

    if (adminActionsBar) adminActionsBar.style.display = isAdmin ? 'block' : 'none';
    if (adminTableAction) adminTableAction.style.display = isAdmin ? 'block' : 'none';
}

function showStudentQuickBar(auth) {
    const bar = document.getElementById('student-catalog-bar');
    const nameSpan = document.getElementById('student-portal-name');
    if (bar && nameSpan) {
        bar.style.display = 'block';
        nameSpan.textContent = auth.name || 'Student';
    }
}

// --------------------------------------------------------------
// Student Loans Drawer inside Books Page
// --------------------------------------------------------------
async function loadStudentDrawerLoans() {
    const tbody = document.getElementById('drawer-loans-tbody');
    const quickCount = document.getElementById('student-loans-quick-count');
    if (!tbody || !quickCount) return;

    try {
        const res = await fetch('/api/loans/my-loans');
        const json = await res.json();
        if (res.ok && json.success && Array.isArray(json.data)) {
            studentLoans = json.data;
            const activeCount = studentLoans.filter(l => l.status === 'ISSUED').length;
            quickCount.textContent = activeCount;

            if (studentLoans.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 1rem; color: #166534;">You have not borrowed any books yet. Click "📥 Borrow Book" on any available book below!</td></tr>`;
                return;
            }

            tbody.innerHTML = studentLoans.map((l, idx) => {
                const isIssued = l.status === 'ISSUED';
                const actionBtn = isIssued
                    ? `<button class="btn btn-success btn-sm" onclick="handleDrawerReturn(${l.id})" style="padding: 0.25rem 0.6rem; font-size: 0.78rem;">↩️ Return</button>`
                    : `<span style="color: var(--text-muted); font-size: 0.8rem;">Returned</span>`;

                return `
                    <tr>
                        <td>${idx + 1}</td>
                        <td style="font-weight: 600; color: var(--primary);">${escapeHtml(l.book ? l.book.title : '-')}</td>
                        <td>${escapeHtml(l.book ? l.book.author : '-')}</td>
                        <td>${formatDate(l.issueDate)}</td>
                        <td style="font-weight: 600; color: ${isIssued ? 'var(--warning)' : 'inherit'};">${formatDate(l.dueDate)}</td>
                        <td><span class="badge ${isIssued ? 'badge-issued' : 'badge-returned'}">${l.status}</span></td>
                        <td>${actionBtn}</td>
                    </tr>
                `;
            }).join('');
        }
    } catch (e) {
        console.error('Failed to load student drawer loans', e);
    }
}

function toggleStudentLoansView() {
    const drawer = document.getElementById('student-loans-drawer');
    const btn = document.getElementById('btn-toggle-loans');
    if (!drawer) return;

    if (drawer.style.display === 'none' || drawer.style.display === '') {
        drawer.style.display = 'block';
        if (btn) btn.textContent = '▲ Hide Borrowed Books';
    } else {
        drawer.style.display = 'none';
        const activeCount = studentLoans.filter(l => l.status === 'ISSUED').length;
        if (btn) btn.textContent = `📖 My Borrowed Books (${activeCount})`;
    }
}

async function handleDrawerReturn(loanId) {
    const loan = studentLoans.find(l => l.id === loanId);
    const bookTitle = loan && loan.book ? loan.book.title : 'this book';

    if (!confirm(`Confirm return of "${bookTitle}"?`)) return;

    try {
        const res = await fetch(`/api/loans/student/return/${loanId}`, { method: 'PUT' });
        const json = await res.json();
        if (res.ok && json.success) {
            showAlert(`Book '${bookTitle}' returned successfully!`, 'success');
            await loadStudentDrawerLoans();
            await loadBooks();
        } else {
            showAlert(json.message || 'Failed to return book', 'error');
        }
    } catch (e) {
        showAlert('Error: ' + e.message, 'error');
    }
}

// --------------------------------------------------------------
// Student Action: Borrow Book
// --------------------------------------------------------------
async function handleStudentBorrow(bookId) {
    if (!currentAuth || !currentAuth.authenticated) {
        showAlert('Please login as a student to borrow books', 'info');
        openRoleModal();
        return;
    }

    if (currentAuth.role !== 'STUDENT') {
        showAlert('Only registered students can borrow books online. Use Librarian Portal for staff checkout.', 'info');
        return;
    }

    const book = allBooks.find(b => b.id === bookId);
    const bookTitle = book ? book.title : 'this book';

    if (!confirm(`Do you want to borrow "${bookTitle}" for 14 days?`)) {
        return;
    }

    try {
        const res = await fetch(`/api/loans/student/borrow/${bookId}`, { method: 'POST' });
        const json = await res.json();

        if (res.ok && json.success) {
            showAlert(`Book '${bookTitle}' borrowed successfully! Due in 14 days.`, 'success');
            await loadStudentDrawerLoans();
            await loadBooks();
        } else {
            showAlert(json.message || 'Failed to borrow book', 'error');
        }
    } catch (e) {
        showAlert('Error borrowing book: ' + e.message, 'error');
    }
}

// --------------------------------------------------------------
// Setup Search and Availability Filters
// --------------------------------------------------------------
function setupSearchAndFilters() {
    const searchInput = document.getElementById('search-input');
    let debounceTimer;
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                filterAndRenderBooks();
            }, 180);
        });
    }

    const statusFilter = document.getElementById('status-filter');
    if (statusFilter) {
        statusFilter.addEventListener('change', () => {
            filterAndRenderBooks();
        });
    }
}

// --------------------------------------------------------------
// Fetch Books from Backend Database
// --------------------------------------------------------------
async function loadBooks() {
    const tableBody = document.getElementById('books-tbody');
    tableBody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding: 2rem; color: var(--text-muted);">Loading books from database...</td></tr>`;

    try {
        const res = await fetch('/api/books');
        const json = await res.json();

        if (json.success && Array.isArray(json.data)) {
            allBooks = json.data;
            filterAndRenderBooks();
        } else {
            tableBody.innerHTML = `<tr><td colspan="8" style="text-align:center; color: var(--danger); padding: 2rem;">Failed to load books: ${escapeHtml(json.message)}</td></tr>`;
        }
    } catch (err) {
        console.error('Error fetching books', err);
        tableBody.innerHTML = `<tr><td colspan="8" style="text-align:center; color: var(--danger); padding: 2rem;">Could not connect to backend server. Make sure Spring Boot is running.</td></tr>`;
    }
}

// --------------------------------------------------------------
// Filter and Render Books Table
// --------------------------------------------------------------
function filterAndRenderBooks() {
    const query = (document.getElementById('search-input').value || '').trim().toLowerCase();
    const filterVal = (document.getElementById('status-filter').value || 'ALL');

    let filtered = allBooks.filter(book => {
        const matchesQuery = !query ||
            (book.title && book.title.toLowerCase().includes(query)) ||
            (book.author && book.author.toLowerCase().includes(query)) ||
            (book.category && book.category.toLowerCase().includes(query)) ||
            (book.isbn && book.isbn.toLowerCase().includes(query));

        if (!matchesQuery) return false;

        if (filterVal === 'AVAILABLE') {
            return book.availableCopies > 0;
        } else if (filterVal === 'OUT_OF_STOCK') {
            return book.availableCopies <= 0;
        }
        return true;
    });

    renderBooksTable(filtered);
}

function renderBooksTable(books) {
    const tableBody = document.getElementById('books-tbody');
    const resultCount = document.getElementById('books-count');
    const isAdmin = currentAuth && currentAuth.role === 'ADMIN';
    const isStudent = currentAuth && currentAuth.role === 'STUDENT';

    resultCount.textContent = `Showing ${books.length} of ${allBooks.length} books`;

    if (books.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
                    <div style="font-size: 2rem; margin-bottom: 0.5rem;">📖</div>
                    <strong>No books found matching your criteria.</strong>
                    <p style="font-size: 0.85rem; margin-top: 0.25rem;">Try modifying your search or filter keywords.</p>
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = books.map((b, index) => {
        const isAvailable = b.availableCopies > 0;
        const badgeClass = isAvailable ? 'badge-available' : 'badge-unavailable';
        const badgeText = isAvailable
            ? `Available (${b.availableCopies}/${b.totalCopies})`
            : `All Issued (0/${b.totalCopies})`;

        let actionTd = '';
        if (isAdmin) {
            actionTd = `
                <td>
                    <div style="display: flex; gap: 0.4rem;">
                        <button class="btn btn-outline btn-sm" onclick="openEditBookModal(${b.id})" title="Edit book">✏️</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteBook(${b.id})" title="Delete book">🗑️</button>
                    </div>
                </td>
            `;
        } else if (isStudent) {
            if (isAvailable) {
                actionTd = `<td><button class="btn btn-primary btn-sm" onclick="handleStudentBorrow(${b.id})" style="padding: 0.3rem 0.75rem; font-size: 0.8rem;">📥 Borrow</button></td>`;
            } else {
                actionTd = `<td><button class="btn btn-outline btn-sm" disabled style="opacity: 0.6; cursor: not-allowed; padding: 0.3rem 0.75rem; font-size: 0.8rem;">Out of Stock</button></td>`;
            }
        } else {
            actionTd = `<td><button class="btn btn-outline btn-sm" onclick="openRoleModal()" style="padding: 0.3rem 0.75rem; font-size: 0.8rem;">🔑 Login to Borrow</button></td>`;
        }

        return `
            <tr>
                <td style="color: var(--text-muted); font-weight: 500;">${index + 1}</td>
                <td style="font-weight: 600; color: var(--primary);">${escapeHtml(b.title)}</td>
                <td>${escapeHtml(b.author)}</td>
                <td><span style="background: #f1f5f9; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.85rem;">${escapeHtml(b.category)}</span></td>
                <td style="font-family: monospace; font-size: 0.9rem;">${escapeHtml(b.isbn)}</td>
                <td style="font-weight: 600;">${b.availableCopies} / ${b.totalCopies}</td>
                <td><span class="badge ${badgeClass}">${badgeText}</span></td>
                ${actionTd}
            </tr>
        `;
    }).join('');
}

// --------------------------------------------------------------
// Setup Modal Forms & Handlers
// --------------------------------------------------------------
function setupModalForms() {
    // Add Book Form
    const addBookForm = document.getElementById('add-book-modal-form');
    if (addBookForm) {
        addBookForm.addEventListener('submit', handleAddBookSubmit);
    }

    // Edit Book Form
    const editBookForm = document.getElementById('edit-book-modal-form');
    if (editBookForm) {
        editBookForm.addEventListener('submit', handleEditBookSubmit);
    }

    // Auth Gateway Forms
    const studentLogin = document.getElementById('student-login-form');
    if (studentLogin) studentLogin.addEventListener('submit', handleStudentLogin);

    const studentReg = document.getElementById('student-register-form');
    if (studentReg) studentReg.addEventListener('submit', handleStudentRegister);

    const adminLogin = document.getElementById('admin-login-form');
    if (adminLogin) adminLogin.addEventListener('submit', handleAdminLogin);
}

function setupModalDismissListeners() {
    const roleModal = document.getElementById('role-auth-modal');
    if (roleModal) {
        roleModal.addEventListener('click', (e) => {
            if (e.target === roleModal) closeRoleModal();
        });
    }

    const addModal = document.getElementById('add-book-modal');
    if (addModal) {
        addModal.addEventListener('click', (e) => {
            if (e.target === addModal) closeAddBookModal();
        });
    }

    const editModal = document.getElementById('edit-book-modal');
    if (editModal) {
        editModal.addEventListener('click', (e) => {
            if (e.target === editModal) closeEditBookModal();
        });
    }

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeRoleModal();
            closeAddBookModal();
            closeEditBookModal();
        }
    });
}

// --------------------------------------------------------------
// Admin: Add New Book
// --------------------------------------------------------------
function openAddBookModal() {
    document.getElementById('add-book-modal-form').reset();
    document.getElementById('modal-book-copies').value = '3';
    document.getElementById('add-book-modal').classList.add('active');
    document.getElementById('modal-book-title').focus();
}

function closeAddBookModal() {
    document.getElementById('add-book-modal').classList.remove('active');
}

async function handleAddBookSubmit(e) {
    e.preventDefault();
    const btn = document.getElementById('modal-add-book-btn');
    btn.disabled = true;
    btn.textContent = 'Saving to database...';

    const payload = {
        title: document.getElementById('modal-book-title').value.trim(),
        author: document.getElementById('modal-book-author').value.trim(),
        isbn: document.getElementById('modal-book-isbn').value.trim(),
        category: document.getElementById('modal-book-category').value.trim(),
        totalCopies: parseInt(document.getElementById('modal-book-copies').value, 10)
    };

    try {
        const res = await fetch('/api/books', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();

        if (res.ok && json.success) {
            showAlert(`Book '${payload.title}' added successfully!`, 'success');
            closeAddBookModal();
            await loadBooks();
        } else {
            showAlert(json.message || 'Failed to add book', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Save Book to Database';
    }
}

// --------------------------------------------------------------
// Admin: Edit Book
// --------------------------------------------------------------
function openEditBookModal(bookId) {
    const book = allBooks.find(b => b.id === bookId);
    if (!book) return;

    document.getElementById('edit-book-id').value = book.id;
    document.getElementById('edit-book-title').value = book.title;
    document.getElementById('edit-book-author').value = book.author;
    document.getElementById('edit-book-isbn').value = book.isbn;
    document.getElementById('edit-book-category').value = book.category;
    document.getElementById('edit-book-copies').value = book.totalCopies;

    document.getElementById('edit-book-modal').classList.add('active');
}

function closeEditBookModal() {
    document.getElementById('edit-book-modal').classList.remove('active');
}

async function handleEditBookSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('edit-book-id').value;
    const btn = document.getElementById('update-book-btn');
    btn.disabled = true;

    const payload = {
        title: document.getElementById('edit-book-title').value.trim(),
        author: document.getElementById('edit-book-author').value.trim(),
        isbn: document.getElementById('edit-book-isbn').value.trim(),
        category: document.getElementById('edit-book-category').value.trim(),
        totalCopies: parseInt(document.getElementById('edit-book-copies').value, 10)
    };

    try {
        const res = await fetch(`/api/books/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();

        if (res.ok && json.success) {
            showAlert('Book updated successfully!', 'success');
            closeEditBookModal();
            await loadBooks();
        } else {
            showAlert(json.message || 'Failed to update book', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
    }
}

// --------------------------------------------------------------
// Admin: Delete Book
// --------------------------------------------------------------
async function deleteBook(id) {
    const book = allBooks.find(b => b.id === id);
    const title = book ? book.title : 'this book';

    if (!confirm(`Are you sure you want to delete "${title}"? This cannot be undone.`)) {
        return;
    }

    try {
        const res = await fetch(`/api/books/${id}`, {
            method: 'DELETE'
        });
        const json = await res.json();

        if (res.ok && json.success) {
            showAlert(`Book '${title}' deleted successfully!`, 'success');
            await loadBooks();
        } else {
            showAlert(json.message || 'Failed to delete book', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    }
}

// --------------------------------------------------------------
// Gateway Modal Navigation
// --------------------------------------------------------------
function openRoleModal() {
    showAuthView('role-select');
    document.getElementById('role-auth-modal').classList.add('active');
}

function closeRoleModal() {
    document.getElementById('role-auth-modal').classList.remove('active');
}

function showAuthView(viewName) {
    document.getElementById('view-role-select').style.display = 'none';
    document.getElementById('view-student-login').style.display = 'none';
    document.getElementById('view-student-register').style.display = 'none';
    document.getElementById('view-admin-login').style.display = 'none';

    document.getElementById('student-login-error').style.display = 'none';
    document.getElementById('student-register-error').style.display = 'none';
    document.getElementById('admin-login-error').style.display = 'none';

    if (viewName === 'role-select') {
        document.getElementById('view-role-select').style.display = 'block';
    } else if (viewName === 'student-login') {
        document.getElementById('view-student-login').style.display = 'block';
        document.getElementById('student-login-email').focus();
    } else if (viewName === 'student-register') {
        document.getElementById('view-student-register').style.display = 'block';
        document.getElementById('reg-name').focus();
    } else if (viewName === 'admin-login') {
        document.getElementById('view-admin-login').style.display = 'block';
        document.getElementById('admin-username').focus();
    }
}

// --------------------------------------------------------------
// Gateway Auth Handlers
// --------------------------------------------------------------
async function handleStudentLogin(e) {
    e.preventDefault();
    const btn = document.getElementById('student-login-btn');
    const errBox = document.getElementById('student-login-error');
    errBox.style.display = 'none';
    btn.disabled = true;

    const payload = {
        email: document.getElementById('student-login-email').value.trim(),
        password: document.getElementById('student-login-password').value
    };

    try {
        const res = await fetch('/api/auth/student/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();

        if (res.ok && json.success) {
            showAlert('Login successful! Welcome to Student Portal.', 'success');
            closeRoleModal();
            currentAuth = json.data;
            updateNavbar(currentAuth);
            showStudentQuickBar(currentAuth);
            await loadStudentDrawerLoans();
            adjustAdminControls();
            loadBooks();
        } else {
            errBox.textContent = json.message || 'Failed to login';
            errBox.style.display = 'block';
        }
    } catch (err) {
        errBox.textContent = 'Network error: ' + err.message;
        errBox.style.display = 'block';
    } finally {
        btn.disabled = false;
    }
}

async function handleStudentRegister(e) {
    e.preventDefault();
    const btn = document.getElementById('student-reg-btn');
    const errBox = document.getElementById('student-register-error');
    errBox.style.display = 'none';
    btn.disabled = true;

    const payload = {
        name: document.getElementById('reg-name').value.trim(),
        email: document.getElementById('reg-email').value.trim(),
        phone: document.getElementById('reg-phone').value.trim(),
        password: document.getElementById('reg-password').value
    };

    try {
        const res = await fetch('/api/auth/student/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();

        if (res.ok && json.success) {
            showAlert('Account created successfully! Welcome, ' + payload.name + '!', 'success');
            closeRoleModal();
            currentAuth = json.data;
            updateNavbar(currentAuth);
            showStudentQuickBar(currentAuth);
            await loadStudentDrawerLoans();
            adjustAdminControls();
            loadBooks();
        } else {
            errBox.textContent = json.message || 'Failed to create account';
            errBox.style.display = 'block';
        }
    } catch (err) {
        errBox.textContent = 'Network error: ' + err.message;
        errBox.style.display = 'block';
    } finally {
        btn.disabled = false;
    }
}

async function handleAdminLogin(e) {
    e.preventDefault();
    const btn = document.getElementById('admin-login-btn');
    const errBox = document.getElementById('admin-login-error');
    errBox.style.display = 'none';
    btn.disabled = true;

    const payload = {
        username: document.getElementById('admin-username').value.trim(),
        password: document.getElementById('admin-password').value
    };

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();

        if (res.ok && json.success) {
            showAlert('Admin login successful! Admin controls unlocked.', 'success');
            closeRoleModal();
            currentAuth = json.data;
            updateNavbar(currentAuth);
            adjustAdminControls();
            loadBooks();
        } else {
            errBox.textContent = json.message || 'Invalid username or password';
            errBox.style.display = 'block';
        }
    } catch (err) {
        errBox.textContent = 'Network error: ' + err.message;
        errBox.style.display = 'block';
    } finally {
        btn.disabled = false;
    }
}
