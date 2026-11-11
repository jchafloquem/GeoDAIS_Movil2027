package pe.gob.devida.geodaismovil.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class PhotoStorageException extends RuntimeException {

	private final HttpStatus status;

	public PhotoStorageException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public PhotoStorageException(HttpStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
