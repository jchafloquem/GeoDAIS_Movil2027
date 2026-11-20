package pe.gob.devida.geodaismovil.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pe.gob.devida.geodaismovil.dto.PhotoUploadRequest;
import pe.gob.devida.geodaismovil.service.PhotoStorageService;

class PhotoControllerTests {

	private PhotoStorageService photoStorageService;
	private PhotoController controller;

	@BeforeEach
	void setUp() {
		photoStorageService = org.mockito.Mockito.mock(PhotoStorageService.class);
		controller = new PhotoController(photoStorageService);
	}

	@Test
	void guardaLaFotoYDevuelveLaReferenciaDelBackend() {
		String base64 = "aGVsbG8=";
		String reference = "https://api.example.test/geodaismovil/api/fotos/550e8400-e29b-41d4-a716-446655440000";
		PhotoStorageService.StoredPhoto photo = new PhotoStorageService.StoredPhoto(
				UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
				reference,
				Path.of("photo.jpg"));
		when(photoStorageService.storeBase64(base64, "80173104", "foto1")).thenReturn(photo);

		ResponseEntity<Map<String, String>> response = controller.guardar(
				new PhotoUploadRequest(base64, "80173104", "foto1"));

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(reference, response.getBody().get("reference"));
		verify(photoStorageService).storeBase64(base64, "80173104", "foto1");
	}
}
