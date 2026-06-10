package com.tulicoreria.licoreria.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.imagenes.ruta:${user.dir}/images/}")
    private String rutaImagenes;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String rutaUploads = Paths.get(rutaImagenes).toAbsolutePath().toUri().toString();

        // Primero busca en la carpeta externa de uploads (imágenes subidas por el admin).
        // Si el archivo no existe ahí (p.ej. producto-default.png), cae al classpath
        // src/main/resources/static/images/ como respaldo.
        registry.addResourceHandler("/images/**")
                .addResourceLocations(rutaUploads, "classpath:/static/images/");
    }
}
