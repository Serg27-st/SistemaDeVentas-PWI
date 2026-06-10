/**
 * admin-fuzzy.js — Motor de búsqueda difusa para el panel administrativo
 * ─────────────────────────────────────────────────────────────────────────
 * Expone `window.adminFuzzy` con:
 *   • levenshtein(a, b)            → distancia de edición
 *   • scoreField(field, query)     → puntuación 0–100 para un campo
 *   • matches(query, ...fields)    → true si algún campo supera el umbral
 *   • setupTable(config)           → configura filtrado fuzzy automático
 *
 * Los templates que ya tienen su propio filtrar() solo necesitan cambiar:
 *   campo.includes(q)  →  window.adminFuzzy.matches(q, campo)
 *
 * Para tablas sin scripts propios (ej: clientes), setupTable() lo hace todo.
 */

(function (global) {
  'use strict';

  // ── Levenshtein ────────────────────────────────────────────────────────────
  function levenshtein(a, b) {
    if (!a) return b ? b.length : 0;
    if (!b) return a.length;
    a = a.toLowerCase(); b = b.toLowerCase();
    if (a === b) return 0;
    const m = a.length, n = b.length;
    let prev = Array.from({ length: n + 1 }, (_, i) => i);
    for (let i = 1; i <= m; i++) {
      const curr = [i];
      for (let j = 1; j <= n; j++) {
        curr[j] = a[i - 1] === b[j - 1]
          ? prev[j - 1]
          : 1 + Math.min(prev[j - 1], prev[j], curr[j - 1]);
      }
      prev = curr;
    }
    return prev[n];
  }

  // ── Score por campo ────────────────────────────────────────────────────────
  function scoreField(field, query) {
    if (!field || !query) return 0;
    const f = field.toLowerCase().trim();
    const q = query.toLowerCase().trim();
    if (!q) return 0;

    if (f === q)           return 100;
    if (f.startsWith(q))   return 88;
    if (f.includes(q))     return 72;

    // Matching por tokens (palabras individuales del campo)
    const tokens = f.split(/[\s\-_/,.()+&]+/).filter(Boolean);
    let best = 0;
    for (const t of tokens) {
      if (t === q)            { best = Math.max(best, 100); break; }
      if (t.startsWith(q))   { best = Math.max(best, 80); continue; }
      if (t.includes(q))     { best = Math.max(best, 65); continue; }
      const d = levenshtein(t, q);
      if      (d === 1 && q.length >= 3) best = Math.max(best, 58);
      else if (d === 2 && q.length >= 4) best = Math.max(best, 38);
      else if (d === 3 && q.length >= 6) best = Math.max(best, 18);
    }
    return best;
  }

  const THRESHOLD = 14;

  // ── API pública ────────────────────────────────────────────────────────────
  /**
   * Devuelve true si la query coincide con alguno de los campos dados.
   * Uso en templates: window.adminFuzzy.matches(q, fila.dataset.nombre, fila.dataset.marca)
   */
  function matches(query) {
    if (!query || !query.trim()) return true;          // vacío → mostrar todo
    const q = query.trim();
    const fields = Array.prototype.slice.call(arguments, 1);
    return fields.some(function (f) { return scoreField(f || '', q) >= THRESHOLD; });
  }

  // ── Auto-setup completo para tablas sin scripts propios ────────────────────
  /**
   * setupTable({ tableId, searchId, filaSinResultadosId, selectIds, textFields })
   *
   * textFields: array de nombres de data-* que se buscan con fuzzy texto
   * selectIds: map de { 'filtroEstado': 'estado', 'filtroDoc': 'tipo' } etc.
   */
  function setupTable(cfg) {
    const input     = document.getElementById(cfg.searchId || 'buscador');
    const tabla     = document.getElementById(cfg.tableId);
    const sinRes    = document.getElementById(cfg.filaSinResultadosId || 'filaSinResultados');
    if (!input || !tabla) return;

    const filas = tabla.querySelectorAll('tbody tr[data-nombre], tbody tr[data-comprobante], tbody tr[data-tipo]');

    function filtrar() {
      const q = input.value.trim();
      const selectores = cfg.selectIds || {};
      let visibles = 0;

      filas.forEach(function (fila) {
        // 1. Text fuzzy match sobre campos de texto configurados
        let textOk = true;
        if (q) {
          const textFields = (cfg.textFields || Object.keys(fila.dataset));
          textOk = textFields.some(function (f) {
            return scoreField(fila.dataset[f] || '', q) >= THRESHOLD;
          });
        }

        // 2. Select exact match
        let selectOk = true;
        Object.keys(selectores).forEach(function (selId) {
          const sel = document.getElementById(selId);
          if (!sel || !sel.value) return;
          const dataKey = selectores[selId];
          if (fila.dataset[dataKey] !== sel.value) selectOk = false;
        });

        const show = textOk && selectOk;
        fila.style.display = show ? '' : 'none';
        if (show) visibles++;
      });

      if (sinRes) sinRes.style.display = visibles === 0 ? '' : 'none';
    }

    // Listeners
    input.addEventListener('input', filtrar);

    // Selects
    const selectores = cfg.selectIds || {};
    Object.keys(selectores).forEach(function (selId) {
      const sel = document.getElementById(selId);
      if (sel) sel.addEventListener('change', filtrar);
    });

    // limpiarFiltros global
    global.limpiarFiltros = function () {
      input.value = '';
      Object.keys(selectores).forEach(function (selId) {
        const sel = document.getElementById(selId);
        if (sel) sel.value = '';
      });
      filtrar();
    };
  }

  // ── Publicar ───────────────────────────────────────────────────────────────
  global.adminFuzzy = { levenshtein, scoreField, matches, setupTable, THRESHOLD };

  // ── Auto-inicializar clientes (no tiene scripts propios) ──────────────────
  document.addEventListener('DOMContentLoaded', function () {
    if (document.getElementById('tablaClientes')) {
      setupTable({
        tableId: 'tablaClientes',
        searchId: 'buscador',
        textFields: ['nombre', 'doc'],
        selectIds: {
          'filtroDoc':  'tipo',
          'filtroEdad': 'edad'
        }
      });
      // Badge fuzzy
      addFuzzyBadge('buscador');
    }
  });

  function addFuzzyBadge(inputId) {
    const input = document.getElementById(inputId);
    if (!input || input.parentElement.querySelector('.admin-search-badge')) return;
    input.parentElement.style.position = 'relative';
    const badge = document.createElement('span');
    badge.className = 'admin-search-badge';
    badge.textContent = '✦ Fuzzy';
    badge.title = 'Búsqueda con tolerancia a errores';
    input.parentElement.appendChild(badge);
  }

  // Exponer addFuzzyBadge para que los templates la usen
  global.adminFuzzy.addFuzzyBadge = addFuzzyBadge;

}(window));
