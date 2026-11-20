package pe.gob.devida.geodaismovil.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import pe.gob.devida.geodaismovil.config.PhotoStorageProperties;
import pe.gob.devida.geodaismovil.exception.PhotoStorageException;

@Service
public class PhotoStorageService {

	private static final String PHOTO_ROUTE = "/geodaismovil/api/fotos/";
	private static final Set<String> PHOTO_FIELDS = Set.of(
			"ruta_dni_anverso", "ruta_dni_reverso", "ruta_foto1", "ruta_foto2", "ruta_foto3",
			"ruta_foto4", "ruta_foto5", "ruta_foto6");
	private static final Set<String> PHOTO_KINDS = Set.of(
			"dni_anverso", "dni_reverso", "foto1", "foto2", "foto3", "foto4", "foto5", "foto6");
	private static final long MAX_IMAGE_PIXELS = 20_000_000;

	private final PhotoStorageProperties properties;
	private final HttpClient httpClient;
	private final Path storageDirectory;

	@Autowired
	public PhotoStorageService(PhotoStorageProperties properties) {
		this(properties, HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()))
				.followRedirects(HttpClient.Redirect.NEVER)
				.build());
	}

	PhotoStorageService(PhotoStorageProperties properties, HttpClient httpClient) {
		validateProperties(properties);
		this.properties = properties;
		this.httpClient = httpClient;
		this.storageDirectory = properties.getDirectory().toAbsolutePath().normalize();
	}

	public List<StoredPhoto> storeReferences(Map<String, Object> values) {
		List<StoredPhoto> storedPhotos = new ArrayList<>();
		try {
			for (String field : PHOTO_FIELDS) {
				Object value = values.get(field);
				if (!(value instanceof String reference) || reference.isBlank()
						|| isManagedReference(reference)) {
					continue;
				}
				StoredPhoto photo = store(reference);
				storedPhotos.add(photo);
				values.put(field, photo.reference());
			}
			return List.copyOf(storedPhotos);
		} catch (RuntimeException exception) {
			cleanupAfterFailure(storedPhotos, exception);
			throw exception;
		}
	}

	public void deleteStored(List<StoredPhoto> photos) {
		for (StoredPhoto photo : photos) {
			try {
				Files.deleteIfExists(photo.path());
			} catch (IOException exception) {
				throw new PhotoStorageException(HttpStatus.INTERNAL_SERVER_ERROR,
						"No se pudo limpiar una imagen almacenada.", exception);
			}
		}
	}

	public PhotoContent load(UUID id) {
		for (String extension : List.of("jpg", "png")) {
			Path legacyFile = storageDirectory.resolve(id + "." + extension).normalize();
			if (legacyFile.startsWith(storageDirectory)
					&& Files.isRegularFile(legacyFile, LinkOption.NOFOLLOW_LINKS)) {
				return new PhotoContent(legacyFile,
						extension.equals("jpg") ? MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG);
			}

			if (Files.isDirectory(storageDirectory, LinkOption.NOFOLLOW_LINKS)) {
				try (DirectoryStream<Path> matches = Files.newDirectoryStream(storageDirectory, id + "_*." + extension)) {
					for (Path file : matches) {
						if (file.startsWith(storageDirectory)
								&& Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
							return new PhotoContent(file,
									extension.equals("jpg") ? MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG);
						}
					}
				} catch (IOException exception) {
					throw new PhotoStorageException(HttpStatus.INTERNAL_SERVER_ERROR,
							"No se pudo acceder al directorio de fotografías.", exception);
				}
			}
		}
		throw new PhotoStorageException(HttpStatus.NOT_FOUND, "No se encontró la imagen solicitada.");
	}

	private StoredPhoto store(String reference) {
		URI source = validateSource(reference);
		byte[] imageBytes = download(source);
		ImageFormat imageFormat = validateImage(imageBytes);
		return storeImage(imageBytes, imageFormat);
	}

	public StoredPhoto storeBase64(String encodedImage, String dniProductor, String photoKind) {
		if (encodedImage == null || encodedImage.isBlank()) {
			throw new IllegalArgumentException("El contenido de la foto es obligatorio.");
		}
		if (dniProductor == null || !dniProductor.matches("\\d{8}")) {
			throw new IllegalArgumentException("El DNI del productor debe tener exactamente 8 dígitos.");
		}
		if (photoKind == null || !PHOTO_KINDS.contains(photoKind)) {
			throw new IllegalArgumentException("El tipo de foto debe ser dni_anverso, dni_reverso o foto1 a foto6.");
		}
		long maxEncodedLength = ((properties.getMaxBytes() + 2) / 3) * 4;
		if (encodedImage.length() > maxEncodedLength) {
			throw new IllegalArgumentException("La imagen excede el tamaño máximo permitido.");
		}

		byte[] imageBytes;
		try {
			imageBytes = Base64.getDecoder().decode(encodedImage);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("El contenido de la foto no está codificado en Base64 válido.", exception);
		}
		if (imageBytes.length == 0 || imageBytes.length > properties.getMaxBytes()) {
			throw new IllegalArgumentException("La imagen excede el tamaño máximo permitido o está vacía.");
		}
		return storeImage(imageBytes, validateImage(imageBytes, HttpStatus.BAD_REQUEST),
				"dni_" + dniProductor + "_" + photoKind);
	}

	private StoredPhoto storeImage(byte[] imageBytes, ImageFormat imageFormat) {
		return storeImage(imageBytes, imageFormat, null);
	}

	private StoredPhoto storeImage(byte[] imageBytes, ImageFormat imageFormat, String fileNamePrefix) {
		UUID id = UUID.randomUUID();
		String fileName = (fileNamePrefix == null ? id : id + "_" + fileNamePrefix)
				+ "." + imageFormat.extension();
		Path file = storageDirectory.resolve(fileName).normalize();
		if (!file.startsWith(storageDirectory)) {
			throw new PhotoStorageException(HttpStatus.INTERNAL_SERVER_ERROR,
					"La ruta de almacenamiento configurada no es válida.");
		}

		try {
			Files.createDirectories(storageDirectory);
			Path temporaryFile = Files.createTempFile(storageDirectory, ".photo-", ".tmp");
			try {
				Files.write(temporaryFile, imageBytes, StandardOpenOption.TRUNCATE_EXISTING,
						StandardOpenOption.WRITE);
				try {
					Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE);
				} catch (AtomicMoveNotSupportedException exception) {
					Files.move(temporaryFile, file);
				}
			} catch (IOException exception) {
				try {
					Files.deleteIfExists(temporaryFile);
				} catch (IOException cleanupFailure) {
					exception.addSuppressed(cleanupFailure);
				}
				throw exception;
			}
		} catch (IOException exception) {
			throw new PhotoStorageException(HttpStatus.INTERNAL_SERVER_ERROR,
					"No se pudo guardar la imagen en el filesystem configurado.", exception);
		}

		String baseUrl = properties.getPublicBaseUrl().trim();
		String url = (baseUrl.isEmpty() ? "" : baseUrl.replaceAll("/+$", "")) + PHOTO_ROUTE + id;
		return new StoredPhoto(id, url, file);
	}

	private URI validateSource(String reference) {
		URI uri;
		try {
			uri = URI.create(reference);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("La URL de la imagen no es válida.", exception);
		}
		String host = uri.getHost();
		if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || uri.getUserInfo() != null
				|| uri.getFragment() != null || (uri.getPort() != -1 && uri.getPort() != 443)) {
			throw new IllegalArgumentException("La URL de la imagen debe ser HTTPS y no puede incluir credenciales.");
		}

		boolean allowed = properties.getAllowedHosts().stream()
				.map(value -> value.trim().toLowerCase(Locale.ROOT))
				.anyMatch(allowedHost -> allowedHost.equals(host.toLowerCase(Locale.ROOT)));
		if (!allowed) {
			throw new IllegalArgumentException("El host de la imagen no está permitido.");
		}

		try {
			for (InetAddress address : InetAddress.getAllByName(host)) {
				if (isNonPublicAddress(address)) {
					throw new IllegalArgumentException("La URL de la imagen resuelve a una dirección no pública.");
				}
			}
		} catch (IOException exception) {
			throw new PhotoStorageException(HttpStatus.BAD_GATEWAY,
					"No se pudo resolver el host de la imagen.", exception);
		}
		return uri;
	}

	private byte[] download(URI source) {
		HttpRequest request = HttpRequest.newBuilder(source)
				.timeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
				.header("Accept", "image/jpeg, image/png")
				.GET()
				.build();
		try {
			HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
			try (InputStream body = response.body()) {
				if (response.statusCode() != 200) {
					throw new PhotoStorageException(HttpStatus.BAD_GATEWAY,
							"El servidor de origen no entregó la imagen correctamente.");
				}
				long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
				if (contentLength > properties.getMaxBytes()) {
					throw new IllegalArgumentException("La imagen excede el tamaño máximo permitido.");
				}
				return readBounded(body, properties.getMaxBytes());
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new PhotoStorageException(HttpStatus.BAD_GATEWAY,
					"La descarga de la imagen fue interrumpida.", exception);
		} catch (IOException exception) {
			throw new PhotoStorageException(HttpStatus.BAD_GATEWAY,
					"No se pudo descargar la imagen desde su URL.", exception);
		}
	}

	private byte[] readBounded(InputStream input, long maxBytes) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		long total = 0;
		int read;
		while ((read = input.read(buffer)) != -1) {
			total += read;
			if (total > maxBytes) {
				throw new IllegalArgumentException("La imagen excede el tamaño máximo permitido.");
			}
			output.write(buffer, 0, read);
		}
		return output.toByteArray();
	}

	private ImageFormat validateImage(byte[] bytes) {
		return validateImage(bytes, HttpStatus.BAD_GATEWAY);
	}

	private ImageFormat validateImage(byte[] bytes, HttpStatus invalidImageStatus) {
		try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			if (input == null) {
				throw new IOException("No se pudo leer el contenido de la imagen.");
			}
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				throw new IllegalArgumentException("El contenido descargado no es una imagen JPEG o PNG válida.");
			}
			ImageReader reader = readers.next();
			try {
				reader.setInput(input, true, true);
				String format = reader.getFormatName().toLowerCase(Locale.ROOT);
				if (!format.equals("jpeg") && !format.equals("png")) {
					throw new IllegalArgumentException("Solo se admiten imágenes JPEG o PNG.");
				}
				long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
				if (pixels <= 0 || pixels > MAX_IMAGE_PIXELS) {
					throw new IllegalArgumentException("Las dimensiones de la imagen exceden el límite permitido.");
				}
				reader.read(0);
				return format.equals("jpeg") ? new ImageFormat("jpg") : new ImageFormat("png");
			} finally {
				reader.dispose();
			}
		} catch (IOException exception) {
			throw new PhotoStorageException(invalidImageStatus,
					"El archivo descargado está dañado o no es una imagen válida.", exception);
		}
	}

	private boolean isManagedReference(String reference) {
		try {
			URI uri = URI.create(reference);
			if (!uri.getPath().matches(PHOTO_ROUTE + "[0-9a-fA-F-]{36}")) {
				return false;
			}
			UUID.fromString(uri.getPath().substring(PHOTO_ROUTE.length()));
			String baseUrl = properties.getPublicBaseUrl().trim();
			if (uri.isAbsolute()) {
				return !baseUrl.isEmpty() && URI.create(baseUrl).getAuthority().equalsIgnoreCase(uri.getAuthority());
			}
			return true;
		} catch (IllegalArgumentException | NullPointerException exception) {
			return false;
		}
	}

	private static void validateProperties(PhotoStorageProperties properties) {
		if (properties.getDirectory() == null || properties.getMaxBytes() < 1
				|| properties.getConnectTimeoutSeconds() < 1 || properties.getRequestTimeoutSeconds() < 1) {
			throw new IllegalArgumentException("La configuración de almacenamiento de fotos no es válida.");
		}
		String baseUrl = properties.getPublicBaseUrl().trim();
		if (!baseUrl.isEmpty()) {
			try {
				URI uri = URI.create(baseUrl);
				if (!uri.isAbsolute() || uri.getHost() == null || uri.getUserInfo() != null
						|| uri.getQuery() != null || uri.getFragment() != null
						|| (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme()))) {
					throw new IllegalArgumentException("GEODAIS_PHOTO_PUBLIC_BASE_URL no es válida.");
				}
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("GEODAIS_PHOTO_PUBLIC_BASE_URL no es válida.", exception);
			}
		}
	}

	private static boolean isNonPublicAddress(InetAddress address) {
		if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
				|| address.isSiteLocalAddress() || address.isMulticastAddress()) {
			return true;
		}
		byte[] bytes = address.getAddress();
		if (bytes.length == 4) {
			int first = Byte.toUnsignedInt(bytes[0]);
			int second = Byte.toUnsignedInt(bytes[1]);
			return first == 0 || first == 10 || first == 127
					|| (first == 100 && second >= 64 && second <= 127)
					|| (first == 169 && second == 254)
					|| (first == 172 && second >= 16 && second <= 31)
					|| (first == 192 && second == 168)
					|| (first == 198 && (second == 18 || second == 19))
					|| first >= 224;
		}
		return (bytes[0] & 0xfe) == 0xfc || (Byte.toUnsignedInt(bytes[0]) == 0xfe
				&& (Byte.toUnsignedInt(bytes[1]) & 0xc0) == 0x80);
	}

	private void cleanupAfterFailure(List<StoredPhoto> storedPhotos, RuntimeException original) {
		try {
			deleteStored(storedPhotos);
		} catch (PhotoStorageException cleanupFailure) {
			original.addSuppressed(cleanupFailure);
		}
	}

	private record ImageFormat(String extension) {
	}

	public record StoredPhoto(UUID id, String reference, Path path) {
	}

	public record PhotoContent(Path path, MediaType mediaType) {
	}
}
