package pe.gob.devida.geodaismovil.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class ApiExceptionHandlerTests {

	@Test
	void missingStaticResourceReturnsNotFoundInsteadOfInternalServerError() {
		ApiExceptionHandler handler = new ApiExceptionHandler();

		var response = handler.handleResourceNotFound(
				new NoResourceFoundException(HttpMethod.GET, "/favicon.ico"));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("Recurso no encontrado.", response.getBody().get("error"));
	}
}
