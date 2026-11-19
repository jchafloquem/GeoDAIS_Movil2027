package pe.gob.devida.geodaismovil.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de recursos de respaldo para evitar errores
 * {@code NoResourceFoundException} en el log cuando el navegador o un proxy
 * solicita recursos que no existen como archivos estáticos.
 *
 * <ul>
 *   <li>{@code /favicon.ico} devuelve 204 No Content para evitar que Spring
 *       intente servir un archivo estático inexistente.</li>
 *   <li>{@code /geodais/swagger-ui/} redirige a {@code /swagger-ui.html},
 *       la ruta real configurada para SpringDoc OpenAPI Swagger UI.</li>
 * </ul>
 *
 * <p>El prefijo {@code /geodais} probablemente proviene de un proxy o reverse
 * proxy frente a esta aplicación. Si es posible, configúrelo para que elimine
 * ese prefijo antes de reenviar la solicitud al backend.</p>
 */
@RestController
public class FallbackResourceController {

    /**
     * Devuelve 204 No Content para el favicon para evitar que el
     * {@code ResourceHttpRequestHandler} intente servir un archivo estático
     * que no existe y registre errores {@code NoResourceFoundException}.
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Redirige {@code /geodais/swagger-ui/} a {@code /swagger-ui.html},
     * que es la ruta real configurada mediante
     * {@code springdoc.swagger-ui.path}.
     */
    @GetMapping("/geodais/swagger-ui/")
    public ResponseEntity<Void> swaggerUiRedirect() {
        return ResponseEntity
            .status(HttpStatus.MOVED_PERMANENTLY)
            .header("Location", "/swagger-ui.html")
            .build();
    }

    /**
     * Maneja la solicitud a la ruta raíz {@code /} para evitar que el
     * {@code ResourceHttpRequestHandler} intente servir un recurso estático
     * inexistente y registre errores {@code NoResourceFoundException}.
     * Redirige a Swagger UI para que el usuario tenga un punto de entrada útil.
     */
    @GetMapping("/")
    public ResponseEntity<Void> root() {
        return ResponseEntity
            .status(HttpStatus.MOVED_PERMANENTLY)
            .header("Location", "/swagger-ui.html")
            .build();
    }
}
