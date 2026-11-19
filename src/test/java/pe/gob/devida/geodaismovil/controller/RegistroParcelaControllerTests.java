package pe.gob.devida.geodaismovil.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.locationtech.jts.io.WKTReader;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pe.gob.devida.geodaismovil.dto.RegistroParcelaDto;
import pe.gob.devida.geodaismovil.model.RegistroParcela;
import pe.gob.devida.geodaismovil.repository.RegistroParcelaRepository;

class RegistroParcelaControllerTests {

	private RegistroParcelaRepository repository;
	private RegistroParcelaController controller;

	@BeforeEach
	void setUp() {
		repository = org.mockito.Mockito.mock(RegistroParcelaRepository.class);
		controller = new RegistroParcelaController(repository);
	}

	@Test
	void listaParcelasPaginadas() throws Exception {
		RegistroParcela entity = parcela("parcela-1");
		when(repository.findAllByEliminadoFalse(any()))
				.thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 50), 1));

		ResponseEntity<Map<String, Object>> response = controller.listar(0, 50);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1L, response.getBody().get("total"));
		@SuppressWarnings("unchecked")
		List<RegistroParcelaDto> data = (List<RegistroParcelaDto>) response.getBody().get("data");
		assertEquals("parcela-1", data.get(0).getInternalKey());
		assertEquals("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))", data.get(0).getGeom());
	}

	@Test
	void obtieneParcelaActivaPorClave() throws Exception {
		when(repository.findByInternalKeyAndEliminadoFalse("parcela-1"))
				.thenReturn(Optional.of(parcela("parcela-1")));

		ResponseEntity<RegistroParcelaDto> response = controller.obtener("parcela-1");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("parcela-1", response.getBody().getInternalKey());
		assertEquals("POINT (0.5 0.5)", response.getBody().getCentroide());
	}

	@Test
	void marcaParcelaComoEliminadaLogicamente() {
		when(repository.marcarEliminado(eq("parcela-1"), any(OffsetDateTime.class))).thenReturn(1);

		ResponseEntity<Void> response = controller.eliminar("parcela-1");

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(repository).marcarEliminado(eq("parcela-1"), any(OffsetDateTime.class));
	}

	@Test
	void convierteWktEnGeometriaConSrid4326AlGuardar() throws Exception {
		RegistroParcelaDto dto = new RegistroParcelaDto();
		dto.setInternalKey("parcela-1");
		dto.setGeom("POINT Z (1 2 3)");
		dto.setCentroide("POINT (1 2)");
		when(repository.save(any(RegistroParcela.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		controller.crearRegistro(dto);

		ArgumentCaptor<RegistroParcela> captor = ArgumentCaptor.forClass(RegistroParcela.class);
		verify(repository).save(captor.capture());
		assertEquals("Point", captor.getValue().getGeom().getGeometryType());
		assertEquals(4326, captor.getValue().getGeom().getSRID());
		assertEquals(4326, captor.getValue().getCentroide().getSRID());
	}

	@Test
	void rechazaWktInvalidoAntesDeGuardar() {
		RegistroParcelaDto dto = new RegistroParcelaDto();
		dto.setInternalKey("parcela-1");
		dto.setGeom("POLYGON INVALIDO");

		assertThrows(IllegalArgumentException.class, () -> controller.crearRegistro(dto));
		verifyNoInteractions(repository);
	}

	@Test
	void rechazaGeometriaDeParcelaConCuatroDimensiones() {
		RegistroParcelaDto dto = new RegistroParcelaDto();
		dto.setInternalKey("parcela-1");
		dto.setGeom("POINT ZM (1 2 3 4)");

		assertThrows(IllegalArgumentException.class, () -> controller.crearRegistro(dto));
		verifyNoInteractions(repository);
	}

	@Test
	void rechazaCentroideConTresDimensiones() {
		RegistroParcelaDto dto = new RegistroParcelaDto();
		dto.setInternalKey("parcela-1");
		dto.setGeom("POINT (1 2)");
		dto.setCentroide("POINT Z (1 2 3)");

		assertThrows(IllegalArgumentException.class, () -> controller.crearRegistro(dto));
		verifyNoInteractions(repository);
	}

	@Test
	void rechazaPaginacionFueraDeRango() {
		assertThrows(IllegalArgumentException.class, () -> controller.listar(0, 101));
	}

	private RegistroParcela parcela(String internalKey) throws Exception {
		RegistroParcela entity = new RegistroParcela();
		entity.setInternalKey(internalKey);
		entity.setEliminado(false);
		entity.setGeom(new WKTReader().read("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))"));
		entity.setCentroide((org.locationtech.jts.geom.Point) new WKTReader().read("POINT (0.5 0.5)"));
		return entity;
	}
}
