/**
 * buscador-publico.js — Autocompletado inteligente para la tienda pública
 * ────────────────────────────────────────────────────────────────────────
 * Características:
 *  ✦ Sugerencias visuales con miniatura, nombre, marca y precio
 *  ✦ Fuzzy search (el backend usa distancia de Levenshtein)
 *  ✦ Búsqueda multicriterio: nombre, marca, categoría, volumen, origen
 *  ✦ Historial de búsquedas con localStorage (últimas 5)
 *  ✦ Tendencias populares desde la API /api/buscar/tendencias
 *  ✦ Navegación con teclado ↑ ↓ Enter Esc
 *  ✦ Debounce de 280ms para no saturar el servidor
 *  ✦ Placeholder dinámico con ejemplos rotativos
 */

(function () {
  'use strict';

  // ── Configuración ────────────────────────────────────────────────────────
  const API_URL       = '/api/buscar';
  const TEND_URL      = '/api/buscar/tendencias';
  const HIST_KEY      = 'tl_search_hist';   // localStorage
  const MAX_HIST      = 5;
  const DEBOUNCE_MS   = 280;
  const MIN_CHARS     = 3;
  const IMG_FALLBACK  = '/images/producto-default.png';
  const PLACEHOLDERS  = [
    'Buscar licor…',
    'Ej: Whisky escocés…',
    'Ej: Ron Cartavio…',
    'Ej: 750ml…',
    'Ej: Tequila…',
    'Ej: Vino tinto…',
  ];

  // ── Estado ───────────────────────────────────────────────────────────────
  let dropdown, input, form;
  let debounceTimer   = null;
  let focusedIdx      = -1;
  let lastQuery       = '';
  let tendencias      = [];
  let abortController = null;

  // ── Init ─────────────────────────────────────────────────────────────────
  function init() {
    form  = document.getElementById('public-search-form');
    input = document.getElementById('public-search-input');
    if (!input || !form) return;

    // Crear dropdown
    dropdown = document.createElement('div');
    dropdown.className = 'search-dropdown';
    dropdown.id = 'search-dropdown';
    dropdown.setAttribute('role', 'listbox');
    form.classList.add('smart-search-wrap');
    form.appendChild(dropdown);

    // Placeholder rotativo
    rotatePlaceholder();

    // Eventos del input
    input.setAttribute('autocomplete', 'off');
    input.addEventListener('focus',   onFocus);
    input.addEventListener('input',   onInput);
    input.addEventListener('keydown', onKeydown);

    // Cerrar al hacer clic fuera
    document.addEventListener('click', function (e) {
      if (!form.contains(e.target)) close();
    });

    // Cargar tendencias en background
    fetchTendencias();
  }

  // ── Placeholder rotativo ─────────────────────────────────────────────────
  function rotatePlaceholder() {
    let i = 0;
    setInterval(function () {
      if (document.activeElement !== input) {
        i = (i + 1) % PLACEHOLDERS.length;
        input.placeholder = PLACEHOLDERS[i];
      }
    }, 3000);
  }

  // ── Tendencias ──────────────────────────────────────────────────────────
  async function fetchTendencias() {
    try {
      const res = await fetch(TEND_URL, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
      if (res.ok) tendencias = await res.json();
    } catch (_) {}
  }

  // ── Historial localStorage ───────────────────────────────────────────────
  function getHistory() {
    try { return JSON.parse(localStorage.getItem(HIST_KEY) || '[]'); }
    catch (_) { return []; }
  }

  function saveToHistory(term) {
    if (!term || term.length < 2) return;
    let hist = getHistory().filter(h => h.toLowerCase() !== term.toLowerCase());
    hist.unshift(term);
    hist = hist.slice(0, MAX_HIST);
    try { localStorage.setItem(HIST_KEY, JSON.stringify(hist)); } catch (_) {}
  }

  function removeFromHistory(term) {
    const hist = getHistory().filter(h => h !== term);
    try { localStorage.setItem(HIST_KEY, JSON.stringify(hist)); } catch (_) {}
    if (!input.value.trim()) showIdle();
  }

  // ── Eventos ──────────────────────────────────────────────────────────────
  function onFocus() {
    if (!input.value.trim()) showIdle();
  }

  function onInput() {
    const q = input.value.trim();
    focusedIdx = -1;

    if (!q) { showIdle(); return; }
    if (q.length < MIN_CHARS) { showMinChars(q); return; }

    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => fetchResults(q), DEBOUNCE_MS);
  }

  function onKeydown(e) {
    if (!isOpen()) return;
    const items = dropdown.querySelectorAll('[data-selectable]');

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      focusedIdx = Math.min(focusedIdx + 1, items.length - 1);
      updateFocus(items);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      focusedIdx = Math.max(focusedIdx - 1, -1);
      if (focusedIdx < 0) input.focus();
      else updateFocus(items);
    } else if (e.key === 'Enter') {
      if (focusedIdx >= 0 && items[focusedIdx]) {
        e.preventDefault();
        items[focusedIdx].click();
      } else {
        submitSearch();
      }
    } else if (e.key === 'Escape') {
      close();
      input.blur();
    }
  }

  function updateFocus(items) {
    items.forEach((el, i) => el.classList.toggle('sd-focused', i === focusedIdx));
    if (items[focusedIdx]) items[focusedIdx].scrollIntoView({ block: 'nearest' });
  }

  // ── Obtener resultados ───────────────────────────────────────────────────
  async function fetchResults(q) {
    if (q === lastQuery) return;
    lastQuery = q;

    if (abortController) abortController.abort();
    abortController = new AbortController();

    showLoading();

    try {
      const res = await fetch(`${API_URL}?q=${encodeURIComponent(q)}&limit=8`, {
        signal: abortController.signal,
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      });
      if (!res.ok) throw new Error('Error de red');
      const data = await res.json();
      renderResults(data, q);
    } catch (err) {
      if (err.name !== 'AbortError') renderError();
    }
  }

  // ── Renderizado ──────────────────────────────────────────────────────────
  function showIdle() {
    const hist = getHistory();
    let html = '';

    if (hist.length) {
      html += `<div class="sd-section">
        <div class="sd-section-title"><i class="bi bi-clock-history"></i> Búsquedas recientes</div>`;
      hist.forEach(function (term) {
        html += `
          <div class="sd-item" data-selectable data-term="${esc(term)}" onclick="window._sdSelectHistory('${esc(term)}')">
            <span class="sd-item-icon"><i class="bi bi-clock"></i></span>
            <span>${esc(term)}</span>
            <button type="button" style="margin-left:auto;background:none;border:none;color:#9ca3af;font-size:.9rem;padding:0 4px;cursor:pointer;"
                    onclick="event.stopPropagation();window._sdRemoveHistory('${esc(term)}')"
                    title="Eliminar del historial">
              <i class="bi bi-x"></i>
            </button>
          </div>`;
      });
      html += '</div>';
    }

    if (tendencias.length) {
      html += `<div class="sd-section">
        <div class="sd-section-title"><i class="bi bi-fire"></i> Lo más buscado</div>
        <div class="sd-chips">`;
      tendencias.forEach(function (t) {
        html += `<a class="sd-chip" data-selectable href="/catalogo?q=${encodeURIComponent(t)}"
                    onclick="window._sdSaveTerm('${esc(t)}')">
                   <i class="bi bi-hash" style="font-size:.75rem;"></i>${esc(t)}
                 </a>`;
      });
      html += '</div></div>';
    }

    if (!hist.length && !tendencias.length) {
      html = `<div class="sd-empty">
        <i class="bi bi-search"></i>
        Escribe al menos ${MIN_CHARS} letras para buscar
      </div>`;
    }

    setContent(html);
    open();
  }

  function showMinChars(q) {
    setContent(`<div class="sd-empty">
      <i class="bi bi-keyboard"></i>
      Escribe <strong>${MIN_CHARS - q.length}</strong> letra${MIN_CHARS - q.length > 1 ? 's' : ''} más para buscar
    </div>`);
    open();
  }

  function showLoading() {
    setContent(`<div class="sd-loading">
      <div class="sd-spinner"></div>
      Buscando…
    </div>`);
    open();
  }

  function renderResults(items, q) {
    if (!items || items.length === 0) {
      setContent(`<div class="sd-empty">
        <i class="bi bi-emoji-frown"></i>
        Sin resultados para "<strong>${esc(q)}</strong>"
        <div style="font-size:.78rem;margin-top:4px;">Intenta con otro término</div>
      </div>`);
      open();
      return;
    }

    let html = `<div class="sd-section">
      <div class="sd-section-title"><i class="bi bi-box-seam"></i> Productos</div>`;

    items.forEach(function (item) {
      const img = item.urlImagen
        ? `<img class="sd-producto-img" src="${esc(item.urlImagen)}"
               alt="${esc(item.nombre)}" onerror="this.src='${IMG_FALLBACK}'">`
        : `<div class="sd-producto-img-ph"><i class="bi bi-droplet-fill"></i></div>`;

      const precio = item.sinStock
        ? `<span class="sd-producto-agotado">Agotado</span>`
        : `<span class="sd-producto-precio">S/. ${(+item.precioVenta).toFixed(2)}</span>`;

      const volDisplay = item.cantidad != null
        ? (item.cantidad % 1 === 0 ? item.cantidad : item.cantidad) + (item.unidadMedida ? ' ' + item.unidadMedida : '')
        : '';
      const sub = [item.marca, volDisplay, item.categoriaNombre]
        .filter(Boolean).join(' · ');

      html += `
        <a class="sd-producto" data-selectable
           href="/producto/${item.id}"
           onclick="window._sdSaveTerm('${esc(q)}')">
          ${img}
          <div class="sd-producto-info">
            <div class="sd-producto-nombre">${highlight(item.nombre, q)}</div>
            <div class="sd-producto-sub">${esc(sub)}</div>
          </div>
          ${precio}
        </a>`;
    });
    html += '</div>';

    // Footer "Ver todos"
    html += `
      <a class="sd-footer" href="/catalogo?q=${encodeURIComponent(q)}"
         onclick="window._sdSaveTerm('${esc(q)}')">
        <span><i class="bi bi-search me-1"></i>Ver todos los resultados para "<em>${esc(q)}</em>"</span>
        <i class="bi bi-arrow-right-short fs-5"></i>
      </a>`;

    setContent(html);
    open();
  }

  function renderError() {
    setContent(`<div class="sd-empty">
      <i class="bi bi-wifi-off"></i>
      Error al conectar. Pulsa Enter para buscar.
    </div>`);
  }

  // ── Helpers DOM ──────────────────────────────────────────────────────────
  function setContent(html) {
    dropdown.innerHTML = html;
    focusedIdx = -1;
  }

  function open() {
    dropdown.classList.add('visible');
  }

  function close() {
    dropdown.classList.remove('visible');
    focusedIdx = -1;
  }

  function isOpen() {
    return dropdown && dropdown.classList.contains('visible');
  }

  function submitSearch() {
    const q = input.value.trim();
    if (q) {
      saveToHistory(q);
      form.submit();
    }
  }

  // ── Text highlight ───────────────────────────────────────────────────────
  function highlight(text, query) {
    if (!text || !query) return esc(text || '');
    const t = text;
    const re = new RegExp('(' + escapeRe(query) + ')', 'gi');
    return t.replace(re, '<mark class="sd-highlight">$1</mark>');
  }

  function esc(s) {
    return String(s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
                           .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
  }

  function escapeRe(s) {
    return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  // ── Funciones globales (llamadas desde el HTML generado) ─────────────────
  window._sdSelectHistory = function (term) {
    input.value = term;
    saveToHistory(term);
    form.submit();
  };

  window._sdRemoveHistory = function (term) {
    removeFromHistory(term);
  };

  window._sdSaveTerm = function (term) {
    saveToHistory(term);
  };

  // ── Arrancar ─────────────────────────────────────────────────────────────
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

}());
