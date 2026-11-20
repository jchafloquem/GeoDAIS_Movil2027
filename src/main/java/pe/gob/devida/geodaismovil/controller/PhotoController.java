package pe.gob.devida.geodaismovil.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.gob.devida.geodaismovil.dto.PhotoUploadRequest;
import pe.gob.devida.geodaismovil.service.PhotoStorageService;

@RestController
@Tag(name = "Fotos", description = "Carga y lectura de imágenes de parcelas almacenadas en el filesystem")
public class PhotoController {

	private final PhotoStorageService photoStorageService;

	public PhotoController(PhotoStorageService photoStorageService) {
		this.photoStorageService = photoStorageService;
	}

	@PostMapping(path = "/geodaismovil/api/fotos", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Guardar una foto de parcela",
			description = "Recibe una imagen JPEG o PNG codificada en Base64 junto con el DNI del productor y el tipo de foto. La guarda en el filesystem configurado con un nombre que conserva esos datos.")
	@ApiResponse(responseCode = "201", description = "Foto guardada; la referencia devuelta puede enviarse en el registro")
	public ResponseEntity<Map<String, String>> guardar(@RequestBody PhotoUploadRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("El contenido de la foto es obligatorio.");
		}
		PhotoStorageService.StoredPhoto photo = photoStorageService.storeBase64(
				request.data(), request.dniProductor(), request.photoKind());
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("reference", photo.reference()));
	}

	@GetMapping("/geodaismovil/api/fotos/{id}")
	@Operation(
			summary = "Obtener una foto por su identificador único",
			description = "Devuelve una imagen JPEG o PNG almacenada localmente. El UUID funciona como referencia no predecible de acceso.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Imagen encontrada"),
					@ApiResponse(responseCode = "404", description = "No existe una imagen con ese UUID"),
					@ApiResponse(responseCode = "500", description = "No fue posible acceder al filesystem")
			})
	public ResponseEntity<Resource> obtener(
			@Parameter(
					description = "UUID de la foto almacenada, tomado de la ruta /api/fotos/{uuid} en rutaFoto1–rutaFoto6 o rutaDniAnverso/rutaDniReverso. No es el device_uuid del registro.",
					example = "bd4152c7-4e44-4a35-9f35-6a1e54ec2353")
			@PathVariable UUID id) {
		PhotoStorageService.PhotoContent photo = photoStorageService.load(id);
		return ResponseEntity.ok()
				.contentType(photo.mediaType())
				.cacheControl(CacheControl.noStore())
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline")
				.body(new FileSystemResource(photo.path()));
	}
}
