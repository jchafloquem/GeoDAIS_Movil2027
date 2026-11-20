package pe.gob.devida.geodaismovil.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pe.gob.devida.geodaismovil.model.Participante;
import pe.gob.devida.geodaismovil.repository.ParticipanteRepository;

class ParticipanteServiceTests {

    private static final String HEADER = "poa_pta,actividad,departamento,provincia,distrito,"
            + "nombre_lugar_intervencion,categoria_lugar,categoria_comunidad,apellido_paterno,"
            + "apellido_materno,nombres,dni,fecha_nacimiento,sexo,cultivo_asistido";

    private ParticipanteRepository repository;
    private ParticipanteService service;

    @BeforeEach
    void setUp() {
        repository = org.mockito.Mockito.mock(ParticipanteRepository.class);
        service = new ParticipanteService(repository);
    }

    @Test
    void importaRegistrosUsandoColumnasDeLaTablaYSoportaCamposCsvEntreComillas() throws IOException {
        String header = HEADER.replace("poa_pta,actividad,", "actividad,poa_pta,");
        String csv = "\uFEFF" + header + "\n"
                + "\"Asistencia, \"\"tecnica\"\"\",POA 2026,Junin,Chanchamayo,Pichanaqui,"
                + "Centro poblado,Comunidad,Nativa,Perez,Garcia,Ana Maria,12345678,1985-06-15,F,Cafe\n";
        List<Participante> savedParticipants = new ArrayList<>();
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<Participante> participants = invocation.getArgument(0);
            participants.forEach(savedParticipants::add);
            return savedParticipants;
        });

        int importedRows = service.importarCsv(input(csv));

        verify(repository).saveAll(any());
        assertEquals(1, importedRows);
        assertEquals(1, savedParticipants.size());
        assertEquals("Asistencia, \"tecnica\"", savedParticipants.get(0).getActividad());
        assertEquals("12345678", savedParticipants.get(0).getDni());
        assertEquals("1985-06-15", savedParticipants.get(0).getFechaNacimiento().toString());
        assertNull(savedParticipants.get(0).getIdParticipante());
    }

    @Test
    void validaTodasLasFilasAntesDePersistir() {
        String csv = HEADER + "\n"
                + "POA 2026,Actividad,Junin,Chanchamayo,Pichanaqui,Lugar,Comunidad,Nativa,Perez,Garcia,"
                + "Ana Maria,12345678,fecha-invalida,F,Cafe";

        assertThrows(IllegalArgumentException.class, () -> service.importarCsv(input(csv)));
        verifyNoInteractions(repository);
    }

    @Test
    void rechazaEncabezadosQueNoCoincidenConLaTabla() {
        assertThrows(IllegalArgumentException.class,
                () -> service.importarCsv(input("dni,nombres\n12345678,Ana")));
        verifyNoInteractions(repository);
    }

    @Test
    void rechazaValoresQueSuperanElTamanoDeLaColumna() {
        String tooLongPoa = "x".repeat(101);
        String csv = HEADER + "\n" + tooLongPoa
                + ",Actividad,Junin,Chanchamayo,Pichanaqui,Lugar,Comunidad,Nativa,Perez,Garcia,"
                + "Ana Maria,12345678,1985-06-15,F,Cafe";

        assertThrows(IllegalArgumentException.class, () -> service.importarCsv(input(csv)));
        verifyNoInteractions(repository);
    }

    private ByteArrayInputStream input(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
