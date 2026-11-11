package pe.gob.devida.geodaismovil.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import pe.gob.devida.geodaismovil.repository.RegistroParcelaRepository;
import tools.jackson.databind.JsonNode;

@SuppressWarnings("unused")
@Service
public class RegistroParcelaService {

	private static final Pattern DNI_PATTERN = Pattern.compile("\\d{8}");
	private static final Pattern BASE64_DATA_URI_PATTERN = Pattern.compile("(?i)^data:[^,]*;base64,");
	private static final Pattern SUPPORTED_GEOMETRY_PATTERN = Pattern.compile(
			"(?is)^\\s*(POINT|POLYGON|LINESTRING)(?:\\s+(?:Z|M|ZM))?\\s*\\(");
	private static final Map<String, ValueType> FIELDS = fields();

	private final RegistroParcelaRepository repository;
	private final PhotoStorageService photoStorageService;

	public RegistroParcelaService(RegistroParcelaRepository repository, PhotoStorageService photoStorageService) {
		this.repository = repository;
		this.photoStorageService = photoStorageService;
	}

	public void guardar(JsonNode request) {
		Map<String, Object> values = convertirRequest(request);
		validarRequeridos(values);
		var storedPhotos = photoStorageService.storeReferences(values);
		try {
			repository.guardar(values);
		} catch (RuntimeException exception) {
			try {
				photoStorageService.deleteStored(storedPhotos);
			} catch (RuntimeException cleanupFailure) {
				exception.addSuppressed(cleanupFailure);
			}
			throw exception;
		}
	}

	public Map<String, Object> listar(int page, int limit, String dniProductor) {
		if (page < 1 || limit < 1) {
			throw new IllegalArgumentException("page y limit deben ser mayores que cero.");
		}
		return repository.listar(page, limit, dniProductor);
	}

	private Map<String, Object> convertirRequest(JsonNode request) {
		if (request == null || !request.isObject()) {
			throw new IllegalArgumentException("El cuerpo de la solicitud debe ser un objeto JSON.");
		}

		Map<String, Object> values = new LinkedHashMap<>();
		request.properties().forEach(entry -> {
			ValueType type = FIELDS.get(entry.getKey());
			if (type == null) {
				throw new IllegalArgumentException("Campo no admitido: " + entry.getKey());
			}
			values.put(entry.getKey(), convert(entry.getValue(), entry.getKey(), type));
		});
		return values;
	}

	@SuppressWarnings("deprecation")
	private Object convert(JsonNode value, String field, ValueType type) {
		if (value == null || value.isNull()) {
			return null;
		}
		if (!value.isValueNode()) {
			throw new IllegalArgumentException("El campo " + field + " debe ser un valor simple.");
		}

		String text = value.isTextual() ? value.textValue() : value.toString();
		try {
			return switch (type) {
				case STRING, GEOMETRY -> {
					if (!value.isTextual()) {
						throw new IllegalArgumentException("El campo " + field + " debe ser texto.");
					}
					yield text;
				}
				case DECIMAL -> new BigDecimal(text);
				case DATE -> LocalDate.parse(text);
				case TIMESTAMP -> OffsetDateTime.parse(text);
				case BOOLEAN -> {
					if (!value.isBoolean()) {
						throw new IllegalArgumentException("El campo " + field + " debe ser booleano.");
					}
					yield value.booleanValue();
				}
			};
		} catch (NumberFormatException | java.time.format.DateTimeParseException exception) {
			throw new IllegalArgumentException("Formato inválido para el campo " + field + ".", exception);
		}
	}

	private void validarRequeridos(Map<String, Object> values) {
		requireText(values, "internal_key");
		requireText(values, "nombres");
		requireText(values, "geom");
		validateGeometry(values.get("geom").toString());
		validateDni(values, "dni_productor");
		validateDni(values, "profesional_dni");
		validatePhotoReferences(values);

		if (values.get("internal_key").toString().length() > 64) {
			throw new IllegalArgumentException("internal_key no puede exceder 64 caracteres.");
		}
	}

	private void validateGeometry(String wkt) {
		if (!SUPPORTED_GEOMETRY_PATTERN.matcher(wkt).find()) {
			throw new IllegalArgumentException("geom debe ser WKT de tipo POINT, LINESTRING o POLYGON.");
		}
	}

	private void validatePhotoReferences(Map<String, Object> values) {
		values.forEach((field, value) -> {
			if ((field.startsWith("ruta_foto") || field.startsWith("ruta_dni_"))
					&& value instanceof String reference
					&& BASE64_DATA_URI_PATTERN.matcher(reference).find()) {
				throw new IllegalArgumentException(field + " debe ser una ruta o URL, no una imagen Base64.");
			}
		});
	}

	private void validateDni(Map<String, Object> values, String field) {
		requireText(values, field);
		if (!DNI_PATTERN.matcher(values.get(field).toString()).matches()) {
			throw new IllegalArgumentException(field + " debe contener exactamente 8 dígitos.");
		}
	}

	private void requireText(Map<String, Object> values, String field) {
		Object value = values.get(field);
		if (!(value instanceof String text) || !StringUtils.hasText(text)) {
			throw new IllegalArgumentException("El campo " + field + " es obligatorio.");
		}
	}

	private static Map<String, ValueType> fields() {
		Map<String, ValueType> fields = new LinkedHashMap<>();
		add(fields, ValueType.STRING, "internal_key", "codigo_autogenerado", "dni_productor", "n_parcela", "nombres", "apellido_paterno",
				"apellido_materno", "nombre_completo", "sexo", "celular_participante", "tipo_productor",
				"actividad_agraria", "tipo_cultivo", "variedad", "regimen_tenencia", "oficina_zonal", "ubigeo",
				"departamento", "provincia", "distrito", "caserio", "fuente", "datum", "observaciones",
				"profesional_dni", "profesional_nombres", "profesional_apellidos", "profesional_celular",
				"profesional_email", "ruta_dni_anverso", "ruta_dni_reverso", "ruta_foto1", "ruta_foto2",
				"ruta_foto3", "ruta_foto4", "ruta_foto5", "ruta_foto6", "device_uuid");
		add(fields, ValueType.GEOMETRY, "geom", "centroide");
		add(fields, ValueType.DECIMAL, "superficie_midagri", "area_ha", "perimetro_m", "altitud");
		add(fields, ValueType.DATE, "fecha_nacimiento");
		add(fields, ValueType.TIMESTAMP, "fecha_captura", "fecha_creacion", "fecha_actualizacion");
		add(fields, ValueType.BOOLEAN, "eliminado");
		return Map.copyOf(fields);
	}

	private static void add(Map<String, ValueType> fields, ValueType type, String... names) {
		for (String name : names) {
			fields.put(name, type);
		}
	}

	private enum ValueType {
		STRING, DECIMAL, DATE, TIMESTAMP, GEOMETRY, BOOLEAN
	}
}
