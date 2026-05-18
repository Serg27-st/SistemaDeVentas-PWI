package com.tulicoreria.licoreria.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagenService {

    private static final String SUBDIRECTORIO_PRODUCTOS = "productos";
    private static final String URL_BASE = "/images/";
    private static final String[] EXTENSIONES_PERMITIDAS = {".jpg", ".jpeg", ".png", ".webp"};

    @Value("${app.imagenes.ruta:${user.dir}/images/}")
    private String directorioImagenes;

    public String guardarImagenProducto(String nombreCategoria, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        validarArchivo(archivo);

        try {
            String carpetaCategoria = normalizarNombreCarpeta(nombreCategoria);
            Path carpetaDestino = Paths.get(directorioImagenes, SUBDIRECTORIO_PRODUCTOS, carpetaCategoria).toAbsolutePath();
            Files.createDirectories(carpetaDestino);

            String original = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "imagen.jpg";
            String extension = obtenerExtension(original);
            String nombreUnico = "producto_" + UUID.randomUUID() + extension;

            Path destino = carpetaDestino.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return SUBDIRECTORIO_PRODUCTOS + "/" + carpetaCategoria + "/" + nombreUnico;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen: " + e.getMessage(), e);
        }
    }

    public void eliminarImagen(String imagenNombre) {
        if (imagenNombre == null || imagenNombre.isBlank()) {
            return;
        }
        try {
            Path rutaArchivo = Paths.get(directorioImagenes, imagenNombre).toAbsolutePath();
            Files.deleteIfExists(rutaArchivo);
        } catch (IOException ignored) {
            // Se omite para no romper la transacción principal
        }
    }

    public String construirUrl(String imagenNombre) {
        if (imagenNombre == null || imagenNombre.isBlank()) {
            return null;
        }
        return URL_BASE + imagenNombre;
    }

    private String normalizarNombreCarpeta(String nombreCategoria) {
        String base = (nombreCategoria != null && !nombreCategoria.isBlank())
                ? nombreCategoria.trim()
                : "SIN_CATEGORIA";

        String sinAcentos = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return sinAcentos
                .toUpperCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".jpg";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).toLowerCase();
    }

    private void validarArchivo(MultipartFile archivo) {
        String nombre = archivo.getOriginalFilename();
        if (nombre == null) {
            throw new RuntimeException("El archivo cargado no es válido.");
        }

        String ext = obtenerExtension(nombre);
        boolean esValida = false;
        for (String permitida : EXTENSIONES_PERMITIDAS) {
            if (ext.equals(permitida)) {
                esValida = true;
                break;
            }
        }

        if (!esValida) {
            throw new RuntimeException("Formato no permitido. Solo se aceptan: JPG, JPEG, PNG o WEBP.");
        }

        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("La imagen excede el límite permitido de 5MB.");
        }
    }
}
