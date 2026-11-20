// ==============================================================
// Library Management System - Librarian Dashboard JavaScript
// ==============================================================

let currentBooks = [];
let currentMembers = [];
let currentLoans = [];
let activeLoanFilter = 'ALL';

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Initialize Tabs & Forms
    setupTabs();
    setupForms();

    // 2. Check Authentication (Librarian/Admin only)
    const auth = await checkAuthStatus();
    if (!auth || !auth.authenticated || auth.role !== 'ADMIN') {
        openAdminLoginModal();
        return;
    }

    document.getElementById('logged-user-name').textContent = auth.username || 'librarian';

    // 3. Initial Data Load
    loadDashboardData();
});

function loadDashboardData() {
    loadDashboardStats();
    loadBooks();
    loadMembers();
    loadLoans();
}

// --------------------------------------------------------------
// Admin Login Modal Handlers
// --------------------------------------------------------------
function openAdminLoginModal() {
    const modal = document.getElementById('admin-login-modal');
    if (modal) modal.classList.add('active');
}

function closeAdminLoginModal() {
    const modal = document.getElementById('admin-login-modal');
    if (modal) modal.classList.remove('active');
}

function goToHome() {
    window.location.href = 'index.html';
}

async function handleAdminAuthLogin(e) {
    e.preventDefault();
    const btn = document.getElementById('admin-auth-btn');
    const errBox = document.getElementById('librarian-auth-error');
    errBox.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Logging in...';

    const payload = {
        username: document.getElementById('admin-auth-username').value.trim(),
        password: document.getElementById('admin-auth-password').value
    };

    try {
        const res = await apiFetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await parseJsonResponse(res);

        if (res.ok && json.success) {
            showAlert('Librarian login successful! Welcome to Admin Dashboard.', 'success');
            closeAdminLoginModal();
            document.getElementById('logged-user-name').textContent = json.data.username || 'librarian';
            loadDashboardData();
        } else {
            errBox.textContent = json.message || 'Invalid username or password';
            errBox.style.display = 'block';
        }
    } catch (err) {
        errBox.textContent = 'Network error: ' + err.message;
        errBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Login to Portal';
    }
}

// --------------------------------------------------------------
// Tabs Navigation
// --------------------------------------------------------------
function setupTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            tabButtons.forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

            btn.classList.add('active');
            const target = btn.getAttribute('data-tab');
            const tabContent = document.getElementById(`tab-${target}`);
            if (tabContent) tabContent.classList.add('active');

            if (target === 'loans') {
                populateIssueFormDropdowns();
            }
        });
    });
}

// --------------------------------------------------------------
// Setup Forms & Event Handlers
// --------------------------------------------------------------
function setupForms() {
    // Admin Login Modal form
    const authForm = document.getElementById('librarian-auth-form');
    if (authForm) authForm.addEventListener('submit', handleAdminAuthLogin);

    // Add Book Form
    const addBookForm = document.getElementById('add-book-form');
    if (addBookForm) addBookForm.addEventListener('submit', handleAddBook);

    // Edit Book Form
    const editBookForm = document.getElementById('edit-book-form');
    if (editBookForm) editBookForm.addEventListener('submit', handleUpdateBook);

    // Add Member Form
    const addMemberForm = document.getElementById('add-member-form');
    if (addMemberForm) addMemberForm.addEventListener('submit', handleAddMember);

    // Issue Book Form
    const issueForm = document.getElementById('issue-book-form');
    if (issueForm) issueForm.addEventListener('submit', handleIssueBook);

    // Loan Filter Buttons
    document.querySelectorAll('.loan-filter-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.loan-filter-btn').forEach(b => b.classList.replace('btn-primary', 'btn-outline'));
            btn.classList.replace('btn-outline', 'btn-primary');
            activeLoanFilter = btn.getAttribute('data-filter');
            renderLoansTable();
        });
    });

    // Default dates for Issue Form
    const todayStr = new Date().toISOString().split('T')[0];
    const dueDate = new Date();
    dueDate.setDate(dueDate.getDate() + 14);
    const dueDateStr = dueDate.toISOString().split('T')[0];

    const issueDateInput = document.getElementById('issue-date');
    const dueDateInput = document.getElementById('due-date');
    if (issueDateInput) issueDateInput.value = todayStr;
    if (dueDateInput) dueDateInput.value = dueDateStr;

    // Backdrop click & Escape key listeners
    const editModal = document.getElementById('edit-book-modal');
    if (editModal) {
        editModal.addEventListener('click', (e) => {
            if (e.target === editModal) closeEditModal();
        });
    }

    const authModal = document.getElementById('admin-login-modal');
    if (authModal) {
        authModal.addEventListener('click', (e) => {
            if (e.target === authModal) goToHome();
        });
    }

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeEditModal();
        }
    });
}

// --------------------------------------------------------------
// Dashboard Overview Stats
// --------------------------------------------------------------
async function loadDashboardStats() {
    try {
        const res = await apiFetch(`${API_BASE}/api/loans/stats`);
        const json = await parseJsonResponse(res);
        if (json.success && json.data) {
            document.getElementById('stat-total-books').textContent = json.data.totalBooks;
            document.getElementById('stat-available-copies').textContent = json.data.availableCopies;
            document.getElementById('stat-issued-copies').textContent = json.data.issuedCopies;
            document.getElementById('stat-total-members').textContent = json.data.totalMembers;
        }
    } catch (e) {
        console.error('Failed to load stats', e);
    }
}

// --------------------------------------------------------------
// 1. Books Management
// --------------------------------------------------------------
async function loadBooks() {
    try {
        const res = await apiFetch(`${API_BASE}/api/books`);
        const json = await parseJsonResponse(res);
        if (json.success && Array.isArray(json.data)) {
            currentBooks = json.data;
            renderBooksTable();
            populateIssueFormDropdowns();
        }
    } catch (err) {
        console.error('Error fetching books', err);
    }
}

function renderBooksTable() {
    const tbody = document.getElementById('librarian-books-tbody');
    if (!tbody) return;

    if (currentBooks.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem; color: var(--text-muted);">No books found in database. Add your first book above.</td></tr>`;
        return;
    }

    tbody.innerHTML = currentBooks.map((b, idx) => {
        const isAvailable = b.availableCopies > 0;
        const badge = isAvailable
            ? `<span class="badge badge-available">Available (${b.availableCopies}/${b.totalCopies})</span>`
            : `<span class="badge badge-unavailable">All Issued (0/${b.totalCopies})</span>`;

        return `
            <tr>
                <td>${idx + 1}</td>
                <td style="font-weight: 600; color: var(--primary);">${escapeHtml(b.title)}</td>
                <td>${escapeHtml(b.author)}</td>
                <td><span style="background: #f1f5f9; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.85rem;">${escapeHtml(b.category)}</span></td>
                <td style="font-family: monospace;">${escapeHtml(b.isbn)}</td>
                <td>${badge}</td>
                <td>
                    <div style="display: flex; gap: 0.4rem;">
                        <button class="btn btn-outline btn-sm" onclick="openEditModal(${b.id})">✏️ Edit</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteBook(${b.id})">🗑️ Delete</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

async function handleAddBook(e) {
    e.preventDefault();
    const btn = document.getElementById('add-book-btn');
    btn.disabled = true;
    btn.textContent = 'Saving...';

    const payload = {
        title: document.getElementById('book-title').value.trim(),
        author: document.getElementById('book-author').value.trim(),
        isbn: document.getElementById('book-isbn').value.trim(),
        category: document.getElementById('book-category').value.trim(),
        totalCopies: parseInt(document.getElementById('book-copies').value, 10)
    };

    try {
        const res = await apiFetch(`${API_BASE}/api/books`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const json = await parseJsonResponse(res);
        if (res.ok && json.success) {
            showAlert(`Book '${payload.title}' added successfully!`, 'success');
            document.getElementById('add-book-form').reset();
            loadBooks();
            loadDashboardStats();
        } else {
            showAlert(json.message || 'Failed to add book', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Add Book to Database';
    }
}

function openEditModal(bookId) {
    const book = currentBooks.find(b => b.id == bookId);
    if (!book) return;

    document.getElementById('edit-book-id').value = book.id;
    document.getElementById('edit-book-title').value = book.title;
    document.getElementById('edit-book-author').value = book.author;
    document.getElementById('edit-book-isbn').value = book.isbn;
    document.getElementById('edit-book-category').value = book.category;
    document.getElementById('edit-book-copies').value = book.totalCopies;

    document.getElementById('edit-book-modal').classList.add('active');
}

function closeEditModal() {
    document.getElementById('edit-book-modal').classList.remove('active');
}

async function handleUpdateBook(e) {
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
        const res = await apiFetch(`${API_BASE}/api/books/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const json = await parseJsonResponse(res);
        if (res.ok && json.success) {
            showAlert('Book updated successfully!', 'success');
            closeEditModal();
            loadBooks();
            loadDashboardStats();
        } else {
            showAlert(json.message || 'Failed to update book', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
    }
}

async function deleteBook(id) {
    const b = currentBooks.find(x => x.id == id);
    const title = b ? b.title : 'this book';

    if (!confirm(`Are you sure you want to delete "${title}"? This cannot be undone.`)) {
        return;
    }

    try {
        const res = await apiFetch(`${API_BASE}/api/books/${id}`, { method: 'DELETE' });
        const json = await parseJsonResponse(res);
        if (res.ok && json.success) {
            showAlert('Book deleted successfully', 'success');
            loadBooks();
            loadDashboardStats();
        } else {
            showAlert(json.message || 'Failed to delete book', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    }
}

// --------------------------------------------------------------
// 2. Members Management
// --------------------------------------------------------------
async function loadMembers() {
    try {
        const res = await apiFetch(`${API_BASE}/api/members`);
        const json = await parseJsonResponse(res);
        if (json.success && Array.isArray(json.data)) {
            currentMembers = json.data;
            renderMembersTable();
            populateIssueFormDropdowns();
        }
    } catch (err) {
        console.error('Error fetching members', err);
    }
}

function renderMembersTable() {
    const tbody = document.getElementById('librarian-members-tbody');
    if (!tbody) return;

    if (currentMembers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2rem; color: var(--text-muted);">No members registered yet. Register a member above.</td></tr>`;
        return;
    }

    tbody.innerHTML = currentMembers.map((m, idx) => `
        <tr>
            <td>${idx + 1}</td>
            <td style="font-weight: 600;">${escapeHtml(m.name)}</td>
            <td>${escapeHtml(m.email)}</td>
            <td>${escapeHtml(m.phone)}</td>
            <td>${formatDate(m.membershipDate)}</td>
            <td><span class="badge badge-available">${escapeHtml(m.status)}</span></td>
        </tr>
    `).join('');
}

async function handleAddMember(e) {
    e.preventDefault();
    const btn = document.getElementById('add-member-btn');
    btn.disabled = true;

    const payload = {
        name: document.getElementById('member-name').value.trim(),
        email: document.getElementById('member-email').value.trim(),
        phone: document.getElementById('member-phone').value.trim()
    };

    try {
        const res = await apiFetch(`${API_BASE}/api/members`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const json = await parseJsonResponse(res);
        if (res.ok && json.success) {
            showAlert(`Member '${payload.name}' registered successfully!`, 'success');
            document.getElementById('add-member-form').reset();
            loadMembers();
            loadDashboardStats();
        } else {
            showAlert(json.message || 'Failed to add member', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
    }
}

// --------------------------------------------------------------
// 3. Issue & Return Loans Management
// --------------------------------------------------------------
function populateIssueFormDropdowns() {
    const memberSelect = document.getElementById('issue-member-id');
    const bookSelect = document.getElementById('issue-book-id');

    // Populate Members
    if (memberSelect) {
        const selectedMember = memberSelect.value;
        memberSelect.innerHTML = `<option value="">-- Choose Registered Member --</option>` +
            currentMembers.map(m => `
                <option value="${m.id}" ${m.id == selectedMember ? 'selected' : ''}>
                    ${escapeHtml(m.name)} (${escapeHtml(m.email)})
                </option>
            `).join('');
    }

    // Populate Books
    if (bookSelect) {
        const selectedBook = bookSelect.value;
        bookSelect.innerHTML = `<option value="">-- Choose Book to Issue --</option>` +
            currentBooks.map(b => {
                const isAvailable = b.availableCopies > 0;
                const statusSuffix = isAvailable
                    ? `[Available: ${b.availableCopies}/${b.totalCopies}]`
                    : `[OUT OF STOCK - 0 available]`;

                return `
                    <option value="${b.id}" ${!isAvailable ? 'disabled' : ''} ${b.id == selectedBook ? 'selected' : ''}>
                        ${escapeHtml(b.title)} by ${escapeHtml(b.author)} ${statusSuffix}
                    </option>
                `;
            }).join('');
    }
}

async function loadLoans() {
    try {
        const res = await apiFetch(`${API_BASE}/api/loans`);
        const json = await parseJsonResponse(res);
        if (json.success && Array.isArray(json.data)) {
            currentLoans = json.data;
            renderLoansTable();
        }
    } catch (err) {
        console.error('Error fetching loans', err);
    }
}

function renderLoansTable() {
    const tbody = document.getElementById('librarian-loans-tbody');
    if (!tbody) return;

    let filtered = currentLoans;

    if (activeLoanFilter === 'ISSUED') {
        filtered = currentLoans.filter(l => l.status === 'ISSUED');
    } else if (activeLoanFilter === 'RETURNED') {
        filtered = currentLoans.filter(l => l.status === 'RETURNED');
    }

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding: 2rem; color: var(--text-muted);">No loan records found for this filter.</td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map((loan) => {
        const isIssued = loan.status === 'ISSUED';
        const badgeClass = isIssued ? 'badge-issued' : 'badge-returned';
        const actionBtn = isIssued
            ? `<button class="btn btn-success btn-sm" onclick="returnBook(${loan.id})">📥 Return Book</button>`
            : `<span style="color: var(--text-muted); font-size: 0.85rem;">Returned</span>`;

        return `
            <tr>
                <td>${loan.id}</td>
                <td style="font-weight: 600; color: var(--primary);">${escapeHtml(loan.book ? loan.book.title : '-')}</td>
                <td>${escapeHtml(loan.member ? loan.member.name : '-')}</td>
                <td>${formatDate(loan.issueDate)}</td>
                <td style="font-weight: 600;">${formatDate(loan.dueDate)}</td>
                <td>${formatDate(loan.returnDate)}</td>
                <td><span class="badge ${badgeClass}">${loan.status}</span></td>
                <td>${actionBtn}</td>
            </tr>
        `;
    }).join('');
}

async function handleIssueBook(e) {
    e.preventDefault();
    const btn = document.getElementById('issue-book-btn');
    btn.disabled = true;

    const bookId = document.getElementById('issue-book-id').value;
    const memberId = document.getElementById('issue-member-id').value;
    const issueDate = document.getElementById('issue-date').value;
    const dueDate = document.getElementById('due-date').value;
    const notes = document.getElementById('issue-notes').value.trim();

    if (!bookId) {
        showAlert('Please select a book to issue', 'error');
        btn.disabled = false;
        return;
    }

    if (!memberId) {
        showAlert('Please select a registered member', 'error');
        btn.disabled = false;
        return;
    }

    if (!dueDate) {
        showAlert('Please specify a due date', 'error');
        btn.disabled = false;
        return;
    }

    const payload = {
        bookId: bookId,
        memberId: memberId,
        issueDate: issueDate,
        dueDate: dueDate,
        notes: notes
    };

    try {
        const res = await apiFetch(`${API_BASE}/api/loans/issue`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(payload)
        });

        const json = await parseJsonResponse(res);
        if (res.ok && json.success) {
            showAlert('Book issued successfully! Inventory availability updated.', 'success');
            document.getElementById('issue-book-form').reset();
            setupForms(); // reset dates
            // Refresh books, loans and stats
            await loadBooks();
            await loadLoans();
            await loadDashboardStats();
        } else {
            showAlert(json.message || 'Failed to issue book', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
    }
}

async function returnBook(loanId) {
    const l = currentLoans.find(x => x.id == loanId);
    const bookTitle = l && l.book ? l.book.title : 'this book';

    if (!confirm(`Confirm return of book "${bookTitle}"? This will mark the loan as returned and increment available copies.`)) {
        return;
    }

    try {
        const res = await apiFetch(`${API_BASE}/api/loans/${loanId}/return`, {
            method: 'PUT'
        });

        const json = await parseJsonResponse(res);
        if (res.ok && json.success) {
            showAlert(`Book '${bookTitle}' returned successfully! Book copy restored to inventory.`, 'success');
            await loadBooks();
            await loadLoans();
            await loadDashboardStats();
        } else {
            showAlert(json.message || 'Failed to return book', 'error');
        }
    } catch (err) {
        showAlert('Network error: ' + err.message, 'error');
    }
}
