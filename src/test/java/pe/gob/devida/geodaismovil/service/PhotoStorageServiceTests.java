package pe.gob.devida.geodaismovil.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;

import pe.gob.devida.geodaismovil.config.PhotoStorageProperties;

class PhotoStorageServiceTests {

	private static final byte[] PNG_IMAGE = java.util.Base64.getDecoder().decode(
			"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/l7sAAAAASUVORK5CYII=");

	@TempDir
	Path tempDirectory;

	private HttpClient httpClient;
	private PhotoStorageProperties properties;
	private PhotoStorageService service;

	@BeforeEach
	void setUp() {
		httpClient = mock(HttpClient.class);
		properties = new PhotoStorageProperties();
		properties.setDirectory(tempDirectory);
		properties.setPublicBaseUrl("https://api.example.test");
		properties.setAllowedHosts(List.of("8.8.8.8"));
		service = new PhotoStorageService(properties, httpClient);
	}

	@Test
	void descargaYGuardaImagenConUuidEnReferenciaDeApi() throws Exception {
		HttpResponse<java.io.InputStream> response = mock(HttpResponse.class);
		when(response.statusCode()).thenReturn(200);
		when(response.headers()).thenReturn(HttpHeaders.of(Map.of(), (name, value) -> true));
		when(response.body()).thenReturn(new ByteArrayInputStream(PNG_IMAGE));
		when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
		Map<String, Object> values = new java.util.LinkedHashMap<>();
		values.put("ruta_foto1", "https://8.8.8.8/foto.png");

		List<PhotoStorageService.StoredPhoto> stored = service.storeReferences(values);

		assertEquals(1, stored.size());
		UUID id = stored.get(0).id();
		assertEquals("https://api.example.test/geodaismovil/api/fotos/" + id, values.get("ruta_foto1"));
		assertTrue(stored.get(0).path().getParent().getFileName().toString().matches("\\d{4}-\\d{2}-\\d{2}"));
		assertTrue(Files.isRegularFile(stored.get(0).path()));
		assertEquals(MediaType.IMAGE_PNG, service.load(id).mediaType());
	}

	@Test
	void rechazaHostNoConfiguradoAntesDeDescargar() {
		Map<String, Object> values = Map.of("ruta_foto1", "https://untrusted.example/foto.jpg");

		assertThrows(IllegalArgumentException.class, () -> service.storeReferences(values));
		verifyNoInteractions(httpClient);
	}

	@Test
	void conservaUnaReferenciaYaGeneradaPorElServicio() {
		String reference = "https://api.example.test/geodaismovil/api/fotos/"
				+ UUID.randomUUID();
		Map<String, Object> values = Map.of("ruta_foto1", reference);

		List<PhotoStorageService.StoredPhoto> stored = service.storeReferences(values);

		assertTrue(stored.isEmpty());
		verifyNoInteractions(httpClient);
	}

	@Test
	void guardaImagenBase64SinDependerDeUnaUrlExterna() {
		PhotoStorageService.StoredPhoto stored = service.storeBase64(
				java.util.Base64.getEncoder().encodeToString(PNG_IMAGE), "80173104", "foto1");

		assertTrue(stored.reference().startsWith("https://api.example.test/geodaismovil/api/fotos/"));
		assertTrue(stored.path().getFileName().toString()
				.matches(stored.id() + "_dni_80173104_foto1\\.png"));
		assertTrue(stored.path().getParent().getFileName().toString().matches("\\d{4}-\\d{2}-\\d{2}"));
		assertTrue(Files.isRegularFile(stored.path()));
		assertEquals(MediaType.IMAGE_PNG, service.load(stored.id()).mediaType());
		verifyNoInteractions(httpClient);
	}

	@Test
	void sigueLeyendoFotosGuardadasEnElDirectorioRaiz() throws Exception {
		UUID id = UUID.randomUUID();
		Path legacyPhoto = tempDirectory.resolve(id + ".png");
		Files.write(legacyPhoto, PNG_IMAGE);

		assertEquals(legacyPhoto, service.load(id).path());
	}

	@Test
	void rechazaContenidoQueNoEsBase64Valido() {
		assertThrows(IllegalArgumentException.class,
				() -> service.storeBase64("not-base64!", "80173104", "foto1"));
		verifyNoInteractions(httpClient);
	}

	@Test
	void rechazaDniOTipoDeFotoFueraDelFormatoPermitido() {
		String encodedImage = java.util.Base64.getEncoder().encodeToString(PNG_IMAGE);

		assertThrows(IllegalArgumentException.class, () -> service.storeBase64(encodedImage, "1234", "foto1"));
		assertThrows(IllegalArgumentException.class, () -> service.storeBase64(encodedImage, "80173104", "foto7"));
		verifyNoInteractions(httpClient);
	}

	@Test
	void devuelve404ParaUuidSinArchivo() {
		assertThrows(pe.gob.devida.geodaismovil.exception.PhotoStorageException.class,
				() -> service.load(UUID.randomUUID()));
		assertFalse(Files.exists(tempDirectory.resolve("not-a-photo.png")));
	}
}
