/* ================================================================
   TU LICORERÍA — JS Vista Pública v2
   ================================================================ */

document.addEventListener('DOMContentLoaded', () => {
    initNavbarScroll();
    initToast();
    animateCartBadge();
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
