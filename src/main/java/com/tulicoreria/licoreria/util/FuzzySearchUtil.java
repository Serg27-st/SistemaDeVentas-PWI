package com.tulicoreria.licoreria.util;

import com.tulicoreria.licoreria.dto.ProductoResponseDTO;
import java.util.Locale;

/**
 * Utilidades de búsqueda fuzzy basadas en la distancia de Levenshtein.
 * Permite encontrar productos aunque el usuario cometa errores ortográficos
 * o escriba nombres extranjeros de forma aproximada.
 */
public final class FuzzySearchUtil {

    private FuzzySearchUtil() {}

    /** Umbral mínimo de puntuación para considerar un resultado relevante (0–100). */
    public static final double THRESHOLD = 14.0;

    // ── Levenshtein ─────────────────────────────────────────────────────────

    /**
     * Calcula la distancia de edición (Levenshtein) entre dos cadenas.
     * Implementación O(m·n) optimizada en espacio a O(n).
     */
    public static int levenshtein(String a, String b) {
        a = a.toLowerCase(Locale.ROOT);
        b = b.toLowerCase(Locale.ROOT);
        if (a.equals(b)) return 0;
        int m = a.length(), n = b.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        for (int j = 0; j <= n; j++) prev[j] = j;
        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                curr[j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? prev[j - 1]
                        : 1 + Math.min(prev[j - 1], Math.min(prev[j], curr[j - 1]));
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[n];
    }

    // ── Scoring por campo ───────────────────────────────────────────────────

    /**
     * Puntúa un campo de texto contra la consulta del usuario.
     * Devuelve un valor entre 0 y 100.
     * <ul>
     *   <li>100 → coincidencia exacta</li>
     *   <li>88  → el campo empieza con la consulta</li>
     *   <li>72  → el campo contiene la consulta</li>
     *   <li>≤58 → coincidencia difusa por tokens (palabras)</li>
     * </ul>
     */
    public static double scoreField(String field, String query) {
        if (field == null || field.isBlank()) return 0;
        String f = field.toLowerCase(Locale.ROOT).trim();
        String q = query.toLowerCase(Locale.ROOT).trim();
        if (q.isEmpty()) return 0;

        if (f.equals(q))      return 100;
        if (f.startsWith(q))  return 88;
        if (f.contains(q))    return 72;

        // Coincidencia difusa a nivel de token (palabra por palabra)
        String[] tokens = f.split("[\\s\\-_/,.()+&]+");
        double best = 0;
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (token.equals(q))       { best = Math.max(best, 100); break; }
            if (token.startsWith(q))   { best = Math.max(best, 80);  continue; }
            if (token.contains(q))     { best = Math.max(best, 65);  continue; }
            int dist = levenshtein(token, q);
            if      (dist == 1 && q.length() >= 3) best = Math.max(best, 58);
            else if (dist == 2 && q.length() >= 4) best = Math.max(best, 38);
            else if (dist == 3 && q.length() >= 6) best = Math.max(best, 18);
        }
        return best;
    }

    // ── Scoring multicriterio para productos ────────────────────────────────

    /**
     * Calcula la puntuación de relevancia de un producto respecto a una consulta,
     * combinando múltiples campos con pesos distintos.
     * Devuelve un valor entre 0 y 100.
     */
    public static double scoreProducto(ProductoResponseDTO p, String query) {
        double best = 0;
        // Nombre del producto — peso máximo
        best = Math.max(best, scoreField(p.getNombre(), query));
        // Marca — muy relevante
        best = Math.max(best, scoreField(p.getMarca(), query) * 0.93);
        // Categoría — relevante
        best = Math.max(best, scoreField(p.getCategoriaNombre(), query) * 0.87);
        // País de origen — relevante para búsquedas como "Escocés", "Peruano"
        best = Math.max(best, scoreField(p.getPaisOrigen(), query) * 0.72);
        // Descripción — peso bajo (ayuda para palabras clave)
        best = Math.max(best, scoreField(p.getDescripcion(), query) * 0.55);
        // Cantidad + unidad — ej: "750ml", "750 ml", "1L", "1 L"
        if (p.getCantidad() != null) {
            String cantStr = p.getCantidad() % 1 == 0
                    ? String.valueOf(p.getCantidad().intValue())
                    : String.valueOf(p.getCantidad());
            String unidad = p.getUnidadMedida() != null ? p.getUnidadMedida() : "";
            best = Math.max(best, scoreField(cantStr + unidad,        query) * 0.60);
            best = Math.max(best, scoreField(cantStr + " " + unidad,  query) * 0.60);
            best = Math.max(best, scoreField(cantStr,                 query) * 0.55);
            if (!unidad.isEmpty()) {
                best = Math.max(best, scoreField(unidad, query) * 0.40);
            }
        }
        return best;
    }
}
