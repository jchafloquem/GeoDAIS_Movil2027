package com.dais.geodais.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import pe.gob.devida.geodaismovil.repository.RegistroParcelaRepository;

/**
 * Controlador REST para recibir registros de parcela enviados desde la app móvil
 * Ionic/Capacitor (Android).
 * <p>
 * Expone el endpoint {@code POST /registros/single} (contexto raíz, sin prefijo).
 * Acepta el payload completo como {@link Map} para no imponer un DTO rígido, ya
 * que la app envía muchos campos opcionales. Los datos se persisten en
 * PostgreSQL a través de {@link RegistroParcelaRepository} (JdbcTemplate), que
 * realiza un upsert por {@code internal_key} y soporta geometrías PostGIS
 * (WKT {@code POINT Z}, {@code POINT}, etc. en SRID 4326).
 * <p>
 * Las excepciones de validación ({@code IllegalArgumentException}) y de base de
 * datos ({@code DataIntegrityViolationException}, {@code DataAccessException})
 * son manejadas globalmente por {@code ApiExceptionHandler}
 * ({@code @RestControllerAdvice} existente en el proyecto), que retorna HTTP 400
 * para solicitudes inválidas y HTTP 500 para errores de acceso a datos.
 */
@RestController
@RequestMapping("/registros")
@Tag(name = "Registros-de-parcela", description = "Recepción de registros de parcela desde GeoDAIS Móvil")
public class RegistroParcelaController {

    private static final Logger logger = LoggerFactory.getLogger(RegistroParcelaController.class);

    private final RegistroParcelaRepository registroParcelaRepository;

    public RegistroParcelaController(RegistroParcelaRepository registroParcelaRepository) {
        this.registroParcelaRepository = registroParcelaRepository;
    }

    /**
     * Recibe un registro de parcela completo enviado desde la app móvil.
     *
     * @param payload mapa JSON con todos los campos del registro
     *        (internal_key, codigo_autogenerado, dni_productor, nombres,
     *        apellido_paterno, apellido_materno, nombre_completo, fecha_nacimiento,
     *        sexo, celular_participante, tipo_productor, actividad_agraria,
     *        tipo_cultivo, regimen_tenencia, superficie_midagri, oficina_zonal,
     *        ubigeo, departamento, provincia, distrito, caserio, geom, centroide,
     *        area_ha, perimetro_m, altitud, fuente, datum, observaciones,
     *        profesional_dni, profesional_nombres, profesional_apellidos,
     *        profesional_celular, profesional_email, ruta_dni_anverso,
     *        ruta_dni_reverso, ruta_foto1 a ruta_foto6, device_uuid, fecha_captura,
     *        fecha_creacion, fecha_actualizacion, eliminado, etc.)
     * @return HTTP 200 con JSON {@code {"status":"ok","message":"Registro
     *         recibido","internal_key":"..."}}
     */
    @PostMapping("/single")
    @Operation(
        summary = "Recibir un registro de parcela desde la app móvil",
        description = "Almacena un registro de parcela enviado por Ionic/Capacitor. "
            + "Acepta campos opcionales sin DTO rígido. internal_key es obligatorio y se usa como clave de upsert.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Registro de parcela",
                    value = """
                        {
                          "internal_key": "id-local-del-registro",
                          "codigo_autogenerado": "PAR-0001",
                          "dni_productor": "12345678",
                          "nombres": "Ana",
                          "apellido_paterno": "Perez",
                          "apellido_materno": "Garcia",
                          "nombre_completo": "Ana Perez Garcia",
                          "sexo": "F",
                          "celular_participante": "987654321",
                          "geom": "POINT Z (-77.0 -12.0 500)",
                          "centroide": "POINT(-77.0 -12.0)",
                          "datum": "WGS84",
                          "fecha_creacion": "2026-11-03T15:00:00-05:00",
                          "fecha_actualizacion": "2026-11-03T15:00:00-05:00",
                          "eliminado": false
                        }
                        """
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Registro recibido y guardado correctamente"),
            @ApiResponse(responseCode = "400", description = "Payload inválido o campos obligatorios faltantes")
        }
    )
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody Map<String, Object> payload) {
        // --- Validación básica del payload entrante ---
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("El payload JSON no puede estar vacío.");
        }

        Object internalKey = payload.get("internal_key");
        if (internalKey == null || internalKey.toString().isBlank()) {
            throw new IllegalArgumentException("El campo internal_key es obligatorio.");
        }

        if (payload.get("dni_productor") == null || payload.get("dni_productor").toString().isBlank()) {
            throw new IllegalArgumentException("El campo dni_productor es obligatorio.");
        }

        if (payload.get("nombres") == null || payload.get("nombres").toString().isBlank()) {
            throw new IllegalArgumentException("El campo nombres es obligatorio.");
        }

        // --- Loguear la recepción del payload (SLF4J) ---
        // Se evita loguear campos sensibles completos; solo se registra metadato clave.
        logger.info(
            "Registro de parcela recibido desde app móvil: internal_key={}, dni_productor={}, total_campos={}",
            internalKey, payload.get("dni_productor"), payload.size());

        // --- Persistir en PostgreSQL (JdbcTemplate a través del repositorio existente) ---
        // RegistroParcelaRepository.guardar() hace INSERT ... ON CONFLICT (internal_key)
        // y mapea geom/centroide con ST_GeomFromText(?, 4326) para PostGIS.
        registroParcelaRepository.guardar(payload);

        // --- Construir y retornar respuesta de éxito (HTTP 200) ---
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("message", "Registro recibido");
        response.put("internal_key", internalKey.toString());

        return ResponseEntity.ok(response);
    }
}