# TODO - Visualizar /publica e arreglar endpoint

## Información recopilada
- Existe el template `src/main/resources/templates/publica/inicio.html`.
- En `HomeController` actualmente solo existen mappings: `/`, `/dashboard`, `/login`, `/error/403`.
- No existe un `@GetMapping("/inicio")` ni un controlador que renderice `publica/inicio`.
- No existe un endpoint `/catalogo` (usado dentro del template) en los controllers encontrados.

## Plan
1. Crear un endpoint público que renderice `publica/inicio` (ruta `/inicio` y/o `/publica/inicio`).
2. Añadir los atributos necesarios al `Model` para que `inicio.html` no falle (al menos `categorias` y `destacados`).
3. Agregar un endpoint mínimo para `/catalogo` para visualizar desde la portada (si el objetivo es ver la carpeta `/publica` sin errores).
4. Asegurar permisos para que `/inicio` (y `/catalogo`) sean accesibles sin login, según la configuración de Spring Security.

## Archivos a revisar/editar
- `src/main/java/com/tulicoreria/licoreria/controller/HomeController.java` (crear mappings).
- Posiblemente `src/main/java/com/tulicoreria/licoreria/config/*` si Spring Security bloquea estas rutas.

## Follow-up steps
- Ejecutar `.\u0020mvnw.cmd package` para validar build.
- Ejecutar la app y probar:
  - `http://localhost:8081/inicio`
  - `http://localhost:8081/catalogo`
- Si aún “no aparece nada”, revisar consola/logs del startup.

