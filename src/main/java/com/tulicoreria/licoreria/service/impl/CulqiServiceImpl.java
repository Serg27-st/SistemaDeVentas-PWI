package com.tulicoreria.licoreria.service.impl;

import com.tulicoreria.licoreria.exception.ReglaDeNegocioException;
import com.tulicoreria.licoreria.service.CulqiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CulqiServiceImpl implements CulqiService {

    private static final Logger log = LoggerFactory.getLogger(CulqiServiceImpl.class);
    private static final String CULQI_CHARGES_URL = "https://api.culqi.com/v2/charges";

    private static final Pattern ID_PATTERN           = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern USER_MESSAGE_PATTERN = Pattern.compile("\"user_message\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern MESSAGE_PATTERN      = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");

    @Value("${culqi.secret-key:sk_test_REEMPLAZA_CON_TU_CLAVE_SECRETA}")
    private String secretKey;

    @Override
    public String cobrar(String token, BigDecimal montoSoles, String email, String descripcion) {
        int montoEnCentavos = montoSoles
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        String body = String.format(
            "{\"amount\":%d,\"currency_code\":\"PEN\",\"email\":\"%s\"," +
            "\"source_id\":\"%s\",\"description\":\"%s\",\"capture\":true}",
            montoEnCentavos,
            sanitize(email),
            sanitize(token),
            sanitize(descripcion)
        );

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CULQI_CHARGES_URL))
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() == 201) {
                String chargeId = extractJsonString(responseBody, ID_PATTERN);
                log.info("Cargo Culqi exitoso: {} por S/. {}", chargeId, montoSoles);
                return chargeId;
            }

            String errorMsg = extractJsonString(responseBody, USER_MESSAGE_PATTERN);
            if (errorMsg.isEmpty()) errorMsg = extractJsonString(responseBody, MESSAGE_PATTERN);
            if (errorMsg.isEmpty()) errorMsg = "Pago rechazado. Verifica los datos de tu tarjeta.";
            log.warn("Culqi rechazó el cargo: {} — HTTP {}", errorMsg, response.statusCode());
            throw new ReglaDeNegocioException(errorMsg);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al conectar con Culqi: {}", e.getMessage());
            throw new ReglaDeNegocioException("No se pudo procesar el pago. Intenta de nuevo o elige otro método.");
        }
    }

    private static String extractJsonString(String json, Pattern pattern) {
        Matcher m = pattern.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }
}
