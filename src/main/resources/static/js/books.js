// ==============================================================
// Library Management System - Books Catalog JavaScript
// ==============================================================

let allBooks = [];

document.addEventListener('DOMContentLoaded', async () => {
    // Check auth to adjust navbar
    const auth = await checkAuthStatus();
    const navAuthItem = document.getElementById('nav-auth-item');
    if (auth && auth.authenticated) {
        navAuthItem.innerHTML = `
            <div style="display: flex; align-items: center; gap: 0.75rem;">
                <a href="/librarian.html" class="nav-btn">Librarian Dashboard</a>
                <button onclick="logout()" class="btn btn-outline btn-sm">Logout</button>
            </div>
        `;
    }

    loadBooks();

    // Setup search listener
    const searchInput = document.getElementById('search-input');
    let debounceTimer;
    searchInput.addEventListener('input', (e) => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            filterAndRenderBooks();
        }, 200);
    });

    // Setup availability filter
    const statusFilter = document.getElementById('status-filter');
    if (statusFilter) {
        statusFilter.addEventListener('change', () => {
            filterAndRenderBooks();
        });
    }
});

async function loadBooks() {
    const tableBody = document.getElementById('books-tbody');
    tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem; color: var(--text-muted);">Loading books from database...</td></tr>`;

    try {
        const res = await fetch('/api/books');
        const json = await res.json();

        if (json.success && Array.isArray(json.data)) {
            allBooks = json.data;
            filterAndRenderBooks();
        } else {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--danger); padding: 2rem;">Failed to load books: ${escapeHtml(json.message)}</td></tr>`;
        }
    } catch (err) {
        console.error('Error fetching books', err);
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--danger); padding: 2rem;">Could not connect to backend server. Make sure Spring Boot is running.</td></tr>`;
    }
}

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
    resultCount.textContent = `Showing ${books.length} ${books.length === 1 ? 'book' : 'books'}`;

    if (books.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
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
            ? `Available (${b.availableCopies} of ${b.totalCopies})`
            : `All Issued (0 of ${b.totalCopies})`;

        return `
            <tr>
                <td style="color: var(--text-muted); font-weight: 500;">${index + 1}</td>
                <td style="font-weight: 600; color: var(--primary);">${escapeHtml(b.title)}</td>
                <td>${escapeHtml(b.author)}</td>
                <td><span style="background: #f1f5f9; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.85rem;">${escapeHtml(b.category)}</span></td>
                <td style="font-family: monospace; font-size: 0.9rem;">${escapeHtml(b.isbn)}</td>
                <td style="font-weight: 600;">${b.availableCopies} / ${b.totalCopies}</td>
                <td><span class="badge ${badgeClass}">${badgeText}</span></td>
            </tr>
        `;
    }).join('');
}
