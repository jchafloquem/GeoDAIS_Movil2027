package pe.gob.devida.geodaismovil.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
}
