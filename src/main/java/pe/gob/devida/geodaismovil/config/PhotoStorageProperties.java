package pe.gob.devida.geodaismovil.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geodais.photos")
public class PhotoStorageProperties {

	private Path directory = Path.of("/opt/JBOSSDATA/GEODAIS");
	private String publicBaseUrl = "";
	private List<String> allowedHosts = new ArrayList<>();
	private long maxBytes = 10 * 1024 * 1024;
	private int connectTimeoutSeconds = 5;
	private int requestTimeoutSeconds = 20;

	public Path getDirectory() {
		return directory;
	}

	public void setDirectory(Path directory) {
		this.directory = directory;
	}

	public String getPublicBaseUrl() {
		return publicBaseUrl;
	}

	public void setPublicBaseUrl(String publicBaseUrl) {
		this.publicBaseUrl = publicBaseUrl;
	}

	public List<String> getAllowedHosts() {
		return allowedHosts;
	}

	public void setAllowedHosts(List<String> allowedHosts) {
		this.allowedHosts = allowedHosts;
	}

	public long getMaxBytes() {
		return maxBytes;
	}

	public void setMaxBytes(long maxBytes) {
		this.maxBytes = maxBytes;
	}

	public int getConnectTimeoutSeconds() {
		return connectTimeoutSeconds;
	}

	public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
		this.connectTimeoutSeconds = connectTimeoutSeconds;
	}

	public int getRequestTimeoutSeconds() {
		return requestTimeoutSeconds;
	}

	public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
		this.requestTimeoutSeconds = requestTimeoutSeconds;
	}
}
