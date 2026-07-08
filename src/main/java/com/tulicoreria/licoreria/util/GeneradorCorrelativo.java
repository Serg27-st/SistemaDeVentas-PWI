package com.tulicoreria.licoreria.util;

public final class GeneradorCorrelativo {

    private GeneradorCorrelativo() {
    }

    /** Genera el siguiente código con formato "PREFIJO-000001" a partir del último valor emitido. */
    public static String siguiente(String prefijo, String ultimoValor) {
        int siguiente = 1;
        if (ultimoValor != null && ultimoValor.contains("-")) {
            try {
                siguiente = Integer.parseInt(ultimoValor.split("-")[1]) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefijo + "-" + String.format("%06d", siguiente);
    }
}
