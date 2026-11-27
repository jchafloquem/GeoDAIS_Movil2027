package pe.gob.devida.geodaismovil.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = SwaggerUiRoutingTests.TestApplication.class)
class SwaggerUiRoutingTests {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	void servesSwaggerUiAtRootContext() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}

	@Test
	void servesOpenApiDocumentAtRootContext() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk());
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration(excludeName = {
			"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
			"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
			"org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
	})
	@Import(OpenApiConfig.class)
	static class TestApplication {
	}
}
