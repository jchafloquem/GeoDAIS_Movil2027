package pe.gob.devida.geodaismovil.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import pe.gob.devida.geodaismovil.exception.PhotoStorageException;

@SuppressWarnings("unused")
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidRequest(IllegalArgumentException exception) {
		return ResponseEntity.badRequest().body(error(exception.getMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolation(DataIntegrityViolationException exception) {
		logger.warn("Registro rechazado por una restricción de la base de datos.", exception);
		return ResponseEntity.badRequest().body(error("Los datos no cumplen las restricciones de registros_parcela."));
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<Map<String, Object>> handleDatabaseError(DataAccessException exception) {
		logger.error("Error al guardar", exception);
		return ResponseEntity.internalServerError().body(error("No se pudo completar la operación en la base de datos."));
	}

	@ExceptionHandler(PhotoStorageException.class)
	public ResponseEntity<Map<String, Object>> handlePhotoStorageError(PhotoStorageException exception) {
		if (exception.getStatus().is5xxServerError()) {
			logger.error("Error al acceder o almacenar una imagen.", exception);
		} else {
			logger.warn("No se pudo completar la operación de imagen: {}", exception.getMessage());
		}
		return ResponseEntity.status(exception.getStatus()).body(error(exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException exception) {
		String fieldErrors = exception.getBindingResult().getFieldErrors().stream()
			.map(err -> err.getField() + ": " + err.getDefaultMessage())
			.collect(Collectors.joining("; "));
		logger.warn("Errores de validación de campos: {}", fieldErrors);
		return ResponseEntity.badRequest().body(error("Errores de validación: " + fieldErrors));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException exception) {
		logger.warn("Parámetro requerido faltante: {}", exception.getParameterName());
		return ResponseEntity.badRequest().body(error("Falta el parámetro requerido: " + exception.getParameterName()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException exception) {
		logger.warn("Cuerpo de solicitud JSON no legible: {}", exception.getMessage());
		return ResponseEntity.badRequest().body(error("El cuerpo de la solicitud no es un JSON válido."));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		logger.warn("Tipo de argumento no coincide: {} = {}", exception.getName(), exception.getValue());
		return ResponseEntity.badRequest().body(error("El parámetro '" + exception.getName() + "' tiene un formato inválido."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleUnexpected(Exception exception) {
		logger.error("Error interno no controlado", exception);
		return ResponseEntity.internalServerError().body(error("Error interno del servidor."));
	}

	private Map<String, Object> error(String message) {
		return Map.of("success", false, "error", message);
	}

	public static Map<String, Object> success(String message) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("success", true);
		response.put("message", message);
		return response;
	}

	public static Map<String, Object> success(String message, Object data) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("success", true);
		response.put("message", message);
		response.put("data", data);
		return response;
	}
}
