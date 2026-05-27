/* ================================================================
   TU LICORERÍA — JS Vista Pública v2
   ================================================================ */

document.addEventListener('DOMContentLoaded', () => {
    initNavbarScroll();
    initToast();
    animateCartBadge();
    initCatalogViewToggle();
    initScrollReveal();
    initSearchExpand();
});

/* ── Navbar: scroll effect ────────────────────────────────────── */
function initNavbarScroll() {
    const nav = document.getElementById('navbar-publica');
    if (!nav) return;
    const fn = () => nav.classList.toggle('navbar-scrolled', window.scrollY > 50);
    window.addEventListener('scroll', fn, { passive: true });
    fn();
}

/* ── Toast carrito ────────────────────────────────────────────── */
function initToast() {
    const toast  = document.getElementById('toast-carrito');
    const msgSrc = document.getElementById('toast-message');
    if (!toast || !msgSrc || !msgSrc.dataset.msg) return;

    const msgEl = toast.querySelector('.toast-msg');
    if (msgEl) msgEl.textContent = msgSrc.dataset.msg;

    // Pequeño delay para que la animación CSS sea visible
    requestAnimationFrame(() => {
        setTimeout(() => toast.classList.add('show'), 80);
    });

    setTimeout(() => toast.classList.remove('show'), 4000);
}

/* ── Badge carrito: animación al cambiar ─────────────────────── */
function animateCartBadge() {
    const badge = document.querySelector('.badge-carrito');
    if (!badge) return;
    if (parseInt(badge.textContent, 10) > 0) {
        badge.classList.add('bump');
        setTimeout(() => badge.classList.remove('bump'), 500);
    }
}

/* ── Toggle grid / lista en catálogo ─────────────────────────── */
function initCatalogViewToggle() {
    const btnGrid = document.getElementById('btn-grid');
    const btnList = document.getElementById('btn-list');
    const grid    = document.getElementById('productos-grid');
    if (!btnGrid || !btnList || !grid) return;

    const GRID_CLASSES = ['col-6', 'col-md-4'];
    const LIST_CLASS   = 'col-12';
    let currentView    = 'grid';

    btnGrid.addEventListener('click', () => {
        if (currentView === 'grid') return;
        currentView = 'grid';
        setView('grid');
        btnGrid.classList.add('active');
        btnList.classList.remove('active');
    });

    btnList.addEventListener('click', () => {
        if (currentView === 'list') return;
        currentView = 'list';
        setView('list');
        btnList.classList.add('active');
        btnGrid.classList.remove('active');
    });

    function setView(mode) {
        const cols = grid.querySelectorAll('[class*="col-"]');
        cols.forEach(col => {
            if (mode === 'list') {
                GRID_CLASSES.forEach(c => col.classList.remove(c));
                col.classList.add(LIST_CLASS);

                const card   = col.querySelector('.card-producto');
                const imgBox = col.querySelector('.card-img-container');
                if (card)   { card.style.flexDirection = 'row'; card.style.maxHeight = '160px'; }
                if (imgBox) { imgBox.style.width = '130px'; imgBox.style.flexShrink = '0'; imgBox.style.height = '100%'; }
            } else {
                col.classList.remove(LIST_CLASS);
                GRID_CLASSES.forEach(c => col.classList.add(c));

                const card   = col.querySelector('.card-producto');
                const imgBox = col.querySelector('.card-img-container');
                if (card)   { card.style.flexDirection = ''; card.style.maxHeight = ''; }
                if (imgBox) { imgBox.style.width = ''; imgBox.style.flexShrink = ''; imgBox.style.height = ''; }
            }
        });
    }
}

/* ── Scroll reveal: tarjetas de productos ────────────────────── */
function initScrollReveal() {
    if (!('IntersectionObserver' in window)) return;

    const style = document.createElement('style');
    style.textContent = `
        .reveal-item {
            opacity: 0;
            transform: translateY(22px);
            transition: opacity 0.5s ease, transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
        }
        .reveal-item.revealed {
            opacity: 1;
            transform: translateY(0);
        }
    `;
    document.head.appendChild(style);

    const cards = document.querySelectorAll('.card-producto');
    if (cards.length === 0) return;

    cards.forEach(card => card.closest('[class*="col-"]')?.classList.add('reveal-item'));

    const obs = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                // Pequeño stagger según posición
                const items   = [...document.querySelectorAll('.reveal-item:not(.revealed)')];
                const idx     = items.indexOf(entry.target);
                const delay   = Math.min(idx * 40, 200);
                setTimeout(() => entry.target.classList.add('revealed'), delay);
                obs.unobserve(entry.target);
            }
        });
    }, { threshold: 0.12, rootMargin: '0px 0px -30px 0px' });

    document.querySelectorAll('.reveal-item').forEach(el => obs.observe(el));
}

/* ── Search input: expand on focus (mobile) ──────────────────── */
function initSearchExpand() {
    const input = document.querySelector('.search-input');
    if (!input) return;
    // La expansión ya se maneja via CSS transition en :focus
    // Aquí solo cerramos el menú móvil al enviar el form en móvil
    const form = input.closest('form');
    if (!form) return;
    form.addEventListener('submit', () => {
        const collapse = document.getElementById('navMenu');
        if (collapse && collapse.classList.contains('show')) {
            const bsCollapse = bootstrap.Collapse.getInstance(collapse);
            bsCollapse?.hide();
        }
    });
}
