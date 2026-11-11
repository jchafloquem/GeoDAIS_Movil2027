package pe.gob.devida.geodaismovil.controller;

import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.gob.devida.geodaismovil.service.PhotoStorageService;

@RestController
@Tag(name = "Fotos", description = "Lectura de imágenes de parcelas almacenadas en el filesystem")
public class PhotoController {

	private final PhotoStorageService photoStorageService;

	public PhotoController(PhotoStorageService photoStorageService) {
		this.photoStorageService = photoStorageService;
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
	public ResponseEntity<Resource> obtener(@PathVariable UUID id) {
		PhotoStorageService.PhotoContent photo = photoStorageService.load(id);
		return ResponseEntity.ok()
				.contentType(photo.mediaType())
				.cacheControl(CacheControl.noStore())
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline")
				.body(new FileSystemResource(photo.path()));
	}
}
