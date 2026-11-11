package pe.gob.devida.geodaismovil.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import tools.jackson.databind.ObjectMapper;

@Repository
public class RegistroParcelaRepository {

	private final JdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper;

	public RegistroParcelaRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = objectMapper;
	}

	public void guardar(Map<String, Object> values) {
		List<String> columns = new ArrayList<>(values.keySet());
		String columnNames = String.join(", ", columns);
		String placeholders = columns.stream()
				.map(RegistroParcelaRepository::placeholder)
				.reduce((left, right) -> left + ", " + right)
				.orElseThrow();
		String updateAssignments = columns.stream()
				.filter(column -> !column.equals("internal_key") && !column.equals("n_parcela"))
				.map(column -> column + " = EXCLUDED." + column)
				.reduce((left, right) -> left + ", " + right)
				.orElse("");

		String sql = "INSERT INTO public.registros_parcela (" + columnNames + ") VALUES (" + placeholders + ") "
				+ "ON CONFLICT (internal_key) "
				+ (updateAssignments.isEmpty() ? "DO NOTHING" : "DO UPDATE SET " + updateAssignments);
		Object[] parameters = columns.stream().map(values::get).toArray();
		jdbcTemplate.update(sql, parameters);
	}

	public Map<String, Object> listar(int page, int limit, String dniProductor) {
		StringBuilder where = new StringBuilder();
		List<Object> filterParameters = new ArrayList<>();
		if (dniProductor != null && !dniProductor.isBlank()) {
			where.append(" WHERE r.dni_productor ILIKE ?");
			filterParameters.add("%" + dniProductor.trim() + "%");
		}

		String select = "SELECT r.internal_key, r.codigo_autogenerado, r.dni_productor, r.n_parcela, r.nombres, "
				+ "r.apellido_paterno, r.apellido_materno, r.nombre_completo, r.fecha_nacimiento, r.sexo, "
				+ "r.celular_participante, r.tipo_productor, r.actividad_agraria, r.tipo_cultivo, r.variedad, "
				+ "r.regimen_tenencia, r.superficie_midagri, r.oficina_zonal, r.ubigeo, r.departamento, "
				+ "r.provincia, r.distrito, r.caserio, ST_AsText(r.geom) AS geom, "
				+ "ST_AsText(r.centroide) AS centroide, r.area_ha, r.perimetro_m, r.altitud, r.fuente, "
				+ "r.datum, r.observaciones, r.profesional_dni, r.profesional_nombres, "
				+ "r.profesional_apellidos, r.profesional_celular, r.profesional_email, "
				+ "r.ruta_dni_anverso, r.ruta_dni_reverso, r.ruta_foto1, r.ruta_foto2, r.ruta_foto3, "
				+ "r.ruta_foto4, r.ruta_foto5, r.ruta_foto6, r.device_uuid, r.fecha_captura, "
				+ "r.fecha_creacion, r.fecha_actualizacion, r.eliminado, ST_AsGeoJSON(r.geom) AS geojson "
				+ "FROM public.registros_parcela r" + where;
		String registrosSql = select + " ORDER BY r.fecha_creacion DESC LIMIT ? OFFSET ?";
		List<Object> registroParameters = new ArrayList<>(filterParameters);
		registroParameters.add(limit);
		registroParameters.add((long) (page - 1) * limit);

		RowMapper<Map<String, Object>> rowMapper = (resultSet, rowNumber) -> {
			Map<String, Object> registro = new LinkedHashMap<>();
			for (int index = 1; index <= resultSet.getMetaData().getColumnCount(); index++) {
				String column = resultSet.getMetaData().getColumnLabel(index);
				Object value = resultSet.getObject(index);
				if ("geojson".equals(column) && value != null) {
					value = objectMapper.readTree(value.toString());
				}
				registro.put(column, value);
			}
			return registro;
		};
		List<Map<String, Object>> registros = jdbcTemplate.query(registrosSql, rowMapper,
				registroParameters.toArray());

		String totalSql = "SELECT COUNT(*) FROM public.registros_parcela r" + where;
		Long total = jdbcTemplate.queryForObject(totalSql, Long.class, filterParameters.toArray());
		long totalRegistros = total == null ? 0 : total;

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("total", totalRegistros);
		response.put("page", page);
		response.put("limit", limit);
		response.put("totalPages", (int) Math.ceil((double) totalRegistros / limit));
		response.put("data", registros);
		return response;
	}

	private static String placeholder(String column) {
		return switch (column) {
			case "geom", "centroide" -> "ST_GeomFromText(?, 4326)";
			default -> "?";
		};
	}
}
