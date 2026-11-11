package pe.gob.devida.geodaismovil.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import tools.jackson.databind.ObjectMapper;

class RegistroParcelaRepositoryTests {

	private JdbcTemplate jdbcTemplate;
	private RegistroParcelaRepository repository;

	@BeforeEach
	void setUp() {
		jdbcTemplate = mock(JdbcTemplate.class);
		repository = new RegistroParcelaRepository(jdbcTemplate, new ObjectMapper());
	}

	@Test
	void guardaGeometriaConPostgisYHaceUpsertPorInternalKey() {
		when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("internal_key", "test-key");
		values.put("codigo_autogenerado", "PAR-0001");
		values.put("dni_productor", "12345678");
		values.put("n_parcela", "12345678-001");
		values.put("geom", "POLYGON Z ((0 0 0, 1 0 0, 1 1 0, 0 0 0))");
		values.put("centroide", "POINT(0 0)");
		values.put("area_ha", new java.math.BigDecimal("1.25"));
		values.put("perimetro_m", new java.math.BigDecimal("456.78"));
		values.put("fecha_creacion", java.time.OffsetDateTime.parse("2026-11-03T15:00:00-05:00"));
		values.put("fecha_actualizacion", java.time.OffsetDateTime.parse("2026-11-03T15:00:00-05:00"));
		values.put("eliminado", true);

		repository.guardar(values);

		ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object[]> parameters = ArgumentCaptor.forClass(Object[].class);
		verify(jdbcTemplate).update(sql.capture(), parameters.capture());
		assertTrue(sql.getValue().contains("ST_GeomFromText(?, 4326)"));
		assertTrue(sql.getValue().contains("ON CONFLICT (internal_key) DO UPDATE SET"));
		assertTrue(!sql.getValue().contains("n_parcela = EXCLUDED.n_parcela"));
		assertTrue(sql.getValue().contains("codigo_autogenerado = EXCLUDED.codigo_autogenerado"));
		assertTrue(sql.getValue().contains("eliminado = EXCLUDED.eliminado"));
		assertTrue(java.util.List.of(parameters.getValue()).contains(true));
	}

	@Test
	void listaConPaginacionYFiltroParcialPorDni() {
		when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());
		when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(0L);

		Map<String, Object> response = repository.listar(2, 5, "1234");

		assertEquals(0L, response.get("total"));
		assertEquals(0, response.get("totalPages"));
		assertTrue(((List<?>) response.get("data")).isEmpty());

		ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object[]> parameters = ArgumentCaptor.forClass(Object[].class);
		verify(jdbcTemplate).query(sql.capture(), any(RowMapper.class), parameters.capture());
		assertTrue(sql.getValue().contains("r.dni_productor ILIKE ?"));
		assertTrue(sql.getValue().contains("r.n_parcela"));
		assertTrue(sql.getValue().contains("ST_AsGeoJSON(r.geom) AS geojson"));
		assertTrue(java.util.Arrays.equals(parameters.getValue(), new Object[] { "%1234%", 5, 5L }));
	}
}
