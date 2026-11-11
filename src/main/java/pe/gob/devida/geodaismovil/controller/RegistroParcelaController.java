package pe.gob.devida.geodaismovil.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.gob.devida.geodaismovil.dto.RegistroParcelaRequest;
import pe.gob.devida.geodaismovil.service.RegistroParcelaService;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/geodaismovil")
@Tag(name = "Registros-de-parcela", description = "Sincronización y consulta de parcelas registradas desde GeoDAIS Móvil")
public class RegistroParcelaController {

	private final RegistroParcelaService service;

	public RegistroParcelaController(RegistroParcelaService service) {
		this.service = service;
	}

	@PostMapping("/registros/single")
	@Operation(
			summary = "Crear o actualizar un registro de parcela",
			description = "Guarda un registro en public.registros_parcela. Si internal_key ya existe, actualiza los campos recibidos; internal_key es la clave única del registro. codigo_autogenerado es opcional y puede repetirse. dni_productor puede repetirse en distintos registros. n_parcela es generado automáticamente por la base de datos como DNI + secuencial y no necesita enviarse. Son obligatorios internal_key, dni_productor, nombres, geom, profesional_dni, datum, fecha_creacion, fecha_actualizacion y eliminado. DNI debe tener 8 dígitos; el celular del participante debe comenzar en 9 y tener 9 dígitos; sexo admite M o F; ubigeo debe tener 6 dígitos; superficie_midagri, si se informa, debe ser mayor que cero. geom admite WKT POINT, LINESTRING o POLYGON (2D o Z) en SRID 4326; aplica la migración SQL documentada para permitir estos tipos en PostGIS. centroide debe ser WKT POINT en SRID 4326. Las fechas-hora deben incluir offset ISO 8601. Las URLs HTTPS enviadas en los campos ruta_* se descargan desde hosts configurados, se guardan con UUID en el filesystem y se reemplazan por URLs del endpoint de fotos. No se admiten Base64.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
					required = true,
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = RegistroParcelaRequest.class),
							examples = @ExampleObject(name = "Registro de parcela", value = """
									{
									  "internal_key": "id-local-del-registro",
									  "codigo_autogenerado": "PAR-0001",
									  "dni_productor": "12345678",
									  "nombres": "Ana",
									  "apellido_paterno": "Perez",
									  "apellido_materno": "Garcia",
									  "nombre_completo": "Ana Perez Garcia",
									  "fecha_nacimiento": "1985-06-15",
									  "sexo": "F",
									  "celular_participante": "987654321",
									  "tipo_productor": "Agricultor",
									  "actividad_agraria": "Agricultura",
									  "tipo_cultivo": "Cafe",
									  "variedad": "01",
									  "regimen_tenencia": "Propietario",
									  "superficie_midagri": 2.5,
									  "oficina_zonal": "La Merced",
									  "ubigeo": "120101",
									  "departamento": "Junin",
									  "provincia": "Chanchamayo",
									  "distrito": "La Merced",
									  "caserio": "San Juan",
									  "profesional_dni": "87654321",
									  "profesional_nombres": "Luis",
									  "profesional_apellidos": "Gomez Ruiz",
									  "profesional_celular": "912345678",
									  "profesional_email": "luis.gomez@devida.gob.pe",
									  "geom": "POLYGON Z ((-77.00063113 -12.00041125 1250.5, -76.99936887 -12.00041125 1250.5, -76.99936887 -11.99958875 1250.5, -77.00063113 -11.99958875 1250.5, -77.00063113 -12.00041125 1250.5))",
									  "centroide": "POINT(-77 -12)",
									  "area_ha": 1.25,
									  "perimetro_m": 456.78,
									  "altitud": 1250.5,
									  "fuente": "GPS",
									  "datum": "WGS84",
									  "observaciones": "Parcela levantada en campo",
									  "ruta_dni_anverso": "https://almacenamiento/ruta/dni-anverso.jpg",
									  "ruta_dni_reverso": "https://almacenamiento/ruta/dni-reverso.jpg",
									  "ruta_foto1": "https://almacenamiento/ruta/foto1.jpg",
									  "ruta_foto2": "https://almacenamiento/ruta/foto2.jpg",
									  "ruta_foto3": "https://almacenamiento/ruta/foto3.jpg",
									  "ruta_foto4": "https://almacenamiento/ruta/foto4.jpg",
									  "ruta_foto5": "https://almacenamiento/ruta/foto5.jpg",
									  "ruta_foto6": "https://almacenamiento/ruta/foto6.jpg",
									  "device_uuid": "device-uuid",
									  "fecha_captura": "2026-11-03T15:00:00-05:00",
									  "fecha_creacion": "2026-11-03T15:00:00-05:00",
									  "fecha_actualizacion": "2026-11-03T15:00:00-05:00",
									  "eliminado": false
									}
									"""))),
			responses = {
					@ApiResponse(responseCode = "200", description = "Registro guardado correctamente"),
					@ApiResponse(responseCode = "400", description = "Formato inválido o violación de una restricción de la tabla", content = @Content(
							mediaType = "application/json",
							examples = @ExampleObject(value = "{\"success\":false,\"error\":\"Los datos no cumplen las restricciones de registros_parcela.\"}"))),
					@ApiResponse(responseCode = "502", description = "No fue posible obtener una imagen desde el host fuente configurado"),
					@ApiResponse(responseCode = "500", description = "Error al acceder a la base de datos", content = @Content(
							mediaType = "application/json",
							examples = @ExampleObject(value = "{\"success\":false,\"error\":\"No se pudo completar la operación en la base de datos.\"}")))
			})
	public ResponseEntity<Map<String, Object>> guardar(@RequestBody JsonNode request) {
		service.guardar(request);
		return ResponseEntity.ok(Map.of("success", true, "message", "Registro guardado correctamente"));
	}

	@GetMapping("/api/registros")
	@Operation(
			summary = "Listar registros de parcela",
			description = "Devuelve registros paginados, con la geometría también serializada como GeoJSON. Incluye registros marcados como eliminados.",
			parameters = {
					@Parameter(name = "page", in = ParameterIn.QUERY, description = "Número de página (desde 1). Debe ser mayor que cero.", example = "1", schema = @Schema(defaultValue = "1", minimum = "1")),
					@Parameter(name = "limit", in = ParameterIn.QUERY, description = "Cantidad de registros por página. Debe ser mayor que cero.", example = "10", schema = @Schema(defaultValue = "10", minimum = "1")),
					@Parameter(name = "dni_productor", in = ParameterIn.QUERY, description = "Filtro parcial e insensible a mayúsculas/minúsculas. Puede devolver varios registros asociados al mismo DNI.", example = "12345678")
			},
			responses = {
					@ApiResponse(responseCode = "200", description = "Página de registros obtenida", content = @Content(
							mediaType = "application/json",
							examples = @ExampleObject(value = """
									{
									  "total": 1,
									  "page": 1,
									  "limit": 10,
									  "totalPages": 1,
									  "data": [
									    {
									      "internal_key": "id-local-del-registro",
									      "dni_productor": "12345678",
									      "nombres": "Ana",
									      "geojson": {
									        "type": "Polygon",
									        "coordinates": [[[-77.0, -12.0, 0.0], [-76.9, -12.0, 0.0], [-76.9, -11.9, 0.0], [-77.0, -12.0, 0.0]]]
									      }
									    }
									  ]
									}
									"""))),
					@ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
			})
	public ResponseEntity<Map<String, Object>> listar(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit,
			@RequestParam(required = false, name = "dni_productor") String dniProductor) {
		return ResponseEntity.ok(service.listar(page, limit, dniProductor));
	}
}
