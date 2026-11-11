package pe.gob.devida.geodaismovil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItems;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
		classes = GeoDaisMovilApplication.class,
		properties = {
				"spring.datasource.username=test",
				"spring.datasource.password=test"
		})
@AutoConfigureMockMvc
class GeoDaisMovilApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void openApiDocumentIncludesParcelaEndpoints() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("GeoDAIS Móvil API"))
				.andExpect(jsonPath("$.paths['/geodaismovil/registros/single'].post").exists())
				.andExpect(jsonPath("$.paths['/geodaismovil/registros/single'].post.tags[0]")
						.value("Registros-de-parcela"))
				.andExpect(jsonPath("$.paths['/api/parcelas'].post.summary")
						.value("Crear o actualizar un registro de parcela"))
				.andExpect(jsonPath("$.paths['/api/parcelas'].post.requestBody.required").value(true))
				.andExpect(jsonPath("$.paths['/api/parcelas'].post.requestBody.content['application/json'].schema.$ref")
						.value("#/components/schemas/RegistroParcelaRequest"))
				.andExpect(jsonPath("$.paths['/geodaismovil/api/registros'].get").exists())
				.andExpect(jsonPath("$.paths['/geodaismovil/api/fotos/{id}'].get").exists())
				.andExpect(jsonPath("$.components.schemas.RegistroParcelaRequest.properties.internal_key").exists())
				.andExpect(jsonPath("$.components.schemas.RegistroParcelaRequest.properties.n_parcela").exists())
				.andExpect(jsonPath("$.components.schemas.RegistroParcelaRequest.properties.n_parcela.readOnly")
						.value(true))
				.andExpect(jsonPath("$.components.schemas.RegistroParcelaRequest.properties.eliminado.type")
						.value("boolean"))
				.andExpect(jsonPath("$.components.schemas.RegistroParcelaRequest.properties.fecha_captura.format")
						.value("date-time"))
				.andExpect(jsonPath("$.components.schemas.RegistroParcelaRequest.properties.dni_productor.description")
						.value(org.hamcrest.Matchers.containsString("Puede repetirse")))
				.andExpect(jsonPath("$.components.schemas.RegistroParcelaRequest.required")
						.value(hasItems("internal_key", "dni_productor", "nombres", "geom", "profesional_dni",
								"datum", "fecha_creacion", "fecha_actualizacion", "eliminado")))
				.andExpect(jsonPath("$.components.schemas.RegistroParcelaRequest.required")
						.value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItems(
								"ruta_dni_anverso", "ruta_dni_reverso", "ruta_foto3", "ruta_foto4",
								"ruta_foto5", "ruta_foto6"))))
				.andExpect(jsonPath("$.paths['/geodaismovil/api/registros'].get.parameters[0].schema.default")
						.value("1"))
				.andExpect(jsonPath("$.paths['/geodaismovil/api/registros'].get.parameters[1].schema.default")
						.value("10"));
	}
}
