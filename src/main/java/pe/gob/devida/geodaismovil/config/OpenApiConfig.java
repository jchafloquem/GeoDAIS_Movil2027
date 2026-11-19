package pe.gob.devida.geodaismovil.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(
		title = "GeoDAIS Móvil API",
		version = "1.0.0",
		description = "API para registrar, consultar y marcar como eliminados lógicamente los registros de parcelas almacenados en public.registros_parcela y consultar participantes de public.participante."))
public class OpenApiConfig {
}
