package pe.gob.devida.geodaismovil.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.gob.devida.geodaismovil.dto.RegistroParcelaRequest;
import pe.gob.devida.geodaismovil.service.RegistroParcelaService;
import tools.jackson.databind.JsonNode;

@RestController
@Tag(name = "Registros-de-parcela", description = "Rutas compatibles para guardar registros de parcela")
public class RegistroParcelaAliasController {

	private final RegistroParcelaService service;

	public RegistroParcelaAliasController(RegistroParcelaService service) {
		this.service = service;
	}

	@PostMapping("/api/parcelas")
	@Operation(
			summary = "Crear o actualizar un registro de parcela",
			description = "Ruta alternativa para guardar en public.registros_parcela. Si internal_key ya existe, actualiza los campos recibidos; internal_key es la clave única del registro. codigo_autogenerado es opcional y puede repetirse. dni_productor puede repetirse en distintos registros. n_parcela es generado automáticamente por la base de datos como DNI + secuencial y no necesita enviarse. Son obligatorios internal_key, dni_productor, nombres, geom, profesional_dni, datum, fecha_creacion, fecha_actualizacion y eliminado. DNI debe tener 8 dígitos; el celular del participante debe comenzar en 9 y tener 9 dígitos; sexo admite M o F; ubigeo debe tener 6 dígitos; superficie_midagri, si se informa, debe ser mayor que cero. geom admite WKT POINT, LINESTRING o POLYGON (2D o Z) en SRID 4326; aplica la migración SQL documentada para permitir estos tipos en PostGIS. centroide debe ser WKT POINT en SRID 4326. Las fechas-hora deben incluir offset ISO 8601. Las URLs HTTPS de ruta_* se descargan desde hosts configurados, se guardan con UUID en el filesystem y se reemplazan por URLs de lectura del API.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
					required = true,
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = RegistroParcelaRequest.class))),
			responses = {
					@ApiResponse(responseCode = "200", description = "Registro guardado correctamente"),
					@ApiResponse(responseCode = "400", description = "Datos inválidos o incompatibles con la tabla"),
					@ApiResponse(responseCode = "502", description = "No fue posible obtener una imagen desde el host fuente configurado"),
					@ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos")
			})
	public ResponseEntity<Map<String, Object>> guardar(@RequestBody JsonNode request) {
		service.guardar(request);
		return ResponseEntity.ok(Map.of("success", true, "message", "Registro guardado correctamente"));
	}
}
