package pe.gob.devida.geodaismovil.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import pe.gob.devida.geodaismovil.model.Participante;
import pe.gob.devida.geodaismovil.service.ParticipanteService;

class ParticipanteControllerTests {

    private ParticipanteService service;
    private ParticipanteController controller;

    @BeforeEach
    void setUp() {
        service = org.mockito.Mockito.mock(ParticipanteService.class);
        controller = new ParticipanteController(service);
    }

    @SuppressWarnings("null")
	@Test
    void buscaParticipantesPorDniYDevuelveTodosSusDatos() {
        List<Participante> participantes = List.of(new Participante());
        when(service.buscarPorDni("12345678")).thenReturn(participantes);

        ResponseEntity<List<Participante>> response = controller.buscarPorDni("12345678");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertSame(participantes.get(0), response.getBody().get(0));
        verify(service).buscarPorDni("12345678");
    }

    @Test
    void rechazaDniVacioSinConsultarElServicio() {
        assertThrows(IllegalArgumentException.class, () -> controller.buscarPorDni("  "));
        verifyNoInteractions(service);
    }

    @Test
    void importaArchivoCsvYDevuelveLaCantidadDeRegistros() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "participantes.csv", "text/csv", "csv".getBytes(StandardCharsets.UTF_8));
        when(service.importarCsv(any(InputStream.class))).thenReturn(2);

        ResponseEntity<Map<String, Object>> response = controller.importarCsv(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("success"));
        assertEquals(2, response.getBody().get("importedRows"));
        verify(service).importarCsv(any(InputStream.class));
    }

    @Test
    void rechazaArchivoCsvVacioSinConsultarElServicio() {
        MockMultipartFile file = new MockMultipartFile("file", "participantes.csv", "text/csv", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> controller.importarCsv(file));
        verifyNoInteractions(service);
    }
}
