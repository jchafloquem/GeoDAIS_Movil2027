package pe.gob.devida.geodaismovil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GeoDaisMovilApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeoDaisMovilApplication.class, args);
	}

}
