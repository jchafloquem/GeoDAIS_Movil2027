package pe.gob.devida.geodaismovil.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pe.gob.devida.geodaismovil.repository.RegistroParcelaRepository;
import tools.jackson.databind.ObjectMapper;

class RegistroParcelaServiceTests {

	private RegistroParcelaRepository repository;
	private PhotoStorageService photoStorageService;
	private RegistroParcelaService service;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		repository = mock(RegistroParcelaRepository.class);
		photoStorageService = mock(PhotoStorageService.class);
		objectMapper = new ObjectMapper();
		service = new RegistroParcelaService(repository, photoStorageService);
	}

	@Test
	void validaYDelegaElRegistroAlRepositorio() throws Exception {
		var request = objectMapper.readTree("""
				{
				  "internal_key": "test-key",
				  "codigo_autogenerado": "PAR-0001",
				  "dni_productor": "12345678",
				  "n_parcela": "12345678-001",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POLYGON Z ((0 0 0, 1 0 0, 1 1 0, 0 0 0))",
				  "centroide": "POINT(-77 -12)",
				  "area_ha": 1.25,
				  "perimetro_m": 456.78,
				  "fecha_creacion": "2026-11-03T15:00:00-05:00",
				  "fecha_actualizacion": "2026-11-03T15:01:00-05:00",
				  "eliminado": true,
				  "ruta_foto1": "https://storage.example/foto.jpg"
				}
				""");

		service.guardar(request);

		verify(repository).guardar(argThat(values ->
				"PAR-0001".equals(values.get("codigo_autogenerado"))
						&& "12345678-001".equals(values.get("n_parcela"))
						&& "https://storage.example/foto.jpg".equals(values.get("ruta_foto1"))
						&& Boolean.TRUE.equals(values.get("eliminado"))
						&& values.get("area_ha") instanceof java.math.BigDecimal
						&& values.get("fecha_creacion") instanceof java.time.OffsetDateTime
						&& "POINT(-77 -12)".equals(values.get("centroide"))));
	}

	@Test
	void relacionaLaUrlGeneradaConLaGeometriaDelRegistro() throws Exception {
		String photoUrl = "/geodaismovil/api/fotos/" + UUID.randomUUID();
		when(photoStorageService.storeReferences(anyMap())).thenAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			Map<String, Object> values = invocation.getArgument(0);
			values.put("ruta_foto1", photoUrl);
			return List.of(new PhotoStorageService.StoredPhoto(
					UUID.fromString(photoUrl.substring(photoUrl.lastIndexOf('/') + 1)), photoUrl,
					Path.of("photo.png")));
		});
		var request = objectMapper.readTree("""
				{
				  "internal_key": "photo-key",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POINT(-77 -12)",
				  "ruta_foto1": "https://storage.example/foto.png"
				}
				""");

		service.guardar(request);

		verify(repository).guardar(argThat(values ->
				photoUrl.equals(values.get("ruta_foto1"))
						&& "POINT(-77 -12)".equals(values.get("geom"))));
	}

	@Test
	void eliminaLosArchivosNuevosSiFallaElUpsert() throws Exception {
		String photoUrl = "/geodaismovil/api/fotos/" + UUID.randomUUID();
		PhotoStorageService.StoredPhoto storedPhoto = new PhotoStorageService.StoredPhoto(
				UUID.fromString(photoUrl.substring(photoUrl.lastIndexOf('/') + 1)), photoUrl, Path.of("photo.png"));
		when(photoStorageService.storeReferences(anyMap())).thenReturn(List.of(storedPhoto));
		org.mockito.Mockito.doThrow(new IllegalStateException("database failure"))
				.when(repository).guardar(anyMap());
		var request = objectMapper.readTree("""
				{
				  "internal_key": "photo-key",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POINT(-77 -12)",
				  "ruta_foto1": "https://storage.example/foto.png"
				}
				""");

		assertThrows(IllegalStateException.class, () -> service.guardar(request));

		verify(photoStorageService).deleteStored(List.of(storedPhoto));
	}

	@Test
	void permiteOmitirReferenciasDeDniYFotosOpcionales() throws Exception {
		var request = objectMapper.readTree("""
				{
				  "internal_key": "test-key-without-photos",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POINT(-77 -12)"
				}
				""");

		service.guardar(request);

		verify(repository).guardar(argThat(values ->
				!values.containsKey("ruta_dni_anverso")
						&& !values.containsKey("ruta_dni_reverso")
						&& !values.containsKey("ruta_foto3")
						&& !values.containsKey("ruta_foto4")
						&& !values.containsKey("ruta_foto5")
						&& !values.containsKey("ruta_foto6")));
	}

	@Test
	void rechazaDniInvalidoAntesDeAccederAlRepositorio() throws Exception {
		var request = objectMapper.readTree("""
				{
				  "internal_key": "test-key",
				  "dni_productor": "123",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POLYGON Z ((0 0 0, 1 0 0, 1 1 0, 0 0 0))"
				}
				""");

		assertThrows(IllegalArgumentException.class, () -> service.guardar(request));
		verifyNoInteractions(repository);
	}

	@Test
	void permiteVariosRegistrosParaElMismoDniProductor() throws Exception {
		var firstRequest = objectMapper.readTree("""
				{
				  "internal_key": "test-key-1",
				  "codigo_autogenerado": "PAR-0001",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POLYGON Z ((0 0 0, 1 0 0, 1 1 0, 0 0 0))"
				}
				""");
		var secondRequest = objectMapper.readTree("""
				{
				  "internal_key": "test-key-2",
				  "codigo_autogenerado": "PAR-0002",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POLYGON Z ((2 2 0, 3 2 0, 3 3 0, 2 2 0))"
				}
				""");

		service.guardar(firstRequest);
		service.guardar(secondRequest);

		org.mockito.Mockito.verify(repository, org.mockito.Mockito.times(2)).guardar(argThat(values ->
				"12345678".equals(values.get("dni_productor"))));
	}

	@Test
	void permiteRepetirCodigoAutogeneradoEnRegistrosDistintos() throws Exception {
		var firstRequest = objectMapper.readTree("""
				{
				  "internal_key": "test-key-1",
				  "codigo_autogenerado": "PAR-0001",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POINT(-77 -12)"
				}
				""");
		var secondRequest = objectMapper.readTree("""
				{
				  "internal_key": "test-key-2",
				  "codigo_autogenerado": "PAR-0001",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POINT(-77 -12)"
				}
				""");

		service.guardar(firstRequest);
		service.guardar(secondRequest);

		org.mockito.Mockito.verify(repository, org.mockito.Mockito.times(2)).guardar(argThat(values ->
				"PAR-0001".equals(values.get("codigo_autogenerado"))));
	}

	@Test
	void permiteGeometriaLineString() throws Exception {
		var request = objectMapper.readTree("""
				{
				  "internal_key": "line-key",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "LINESTRING Z (-77 -12 0, -76.9 -11.9 0)"
				}
				""");

		service.guardar(request);

		verify(repository).guardar(argThat(values ->
				"LINESTRING Z (-77 -12 0, -76.9 -11.9 0)".equals(values.get("geom"))));
	}

	@Test
	void permiteGeometriaPoint() throws Exception {
		var request = objectMapper.readTree("""
				{
				  "internal_key": "point-key",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POINT(-77 -12)"
				}
				""");

		service.guardar(request);

		verify(repository).guardar(argThat(values -> "POINT(-77 -12)".equals(values.get("geom"))));
	}

	@Test
	void rechazaTiposDeGeometriaNoAdmitidos() throws Exception {
		var request = objectMapper.readTree("""
				{
				  "internal_key": "multipoint-key",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "MULTIPOINT((-77 -12), (-76 -11))"
				}
				""");

		org.junit.jupiter.api.Assertions.assertEquals(
				"geom debe ser WKT de tipo POINT, LINESTRING o POLYGON.",
				org.junit.jupiter.api.Assertions.assertThrows(
						IllegalArgumentException.class, () -> service.guardar(request)).getMessage());
		verifyNoInteractions(repository);
	}

	@Test
	void rechazaImagenesBase64EnLosCamposDeRutas() throws Exception {
		var request = objectMapper.readTree("""
				{
				  "internal_key": "test-key",
				  "dni_productor": "12345678",
				  "nombres": "Ana",
				  "profesional_dni": "87654321",
				  "geom": "POLYGON Z ((0 0 0, 1 0 0, 1 1 0, 0 0 0))",
				  "ruta_foto1": "data:image/jpeg;base64,abc123"
				}
				""");

		assertThrows(IllegalArgumentException.class, () -> service.guardar(request));
		verifyNoInteractions(repository);
	}

	@Test
	void rechazaParametrosDePaginacionInvalidos() {
		assertThrows(IllegalArgumentException.class, () -> service.listar(0, 10, null));
		verifyNoInteractions(repository);
	}
}
