package pe.gob.devida.geodaismovil.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.gob.devida.geodaismovil.model.Participante;
import pe.gob.devida.geodaismovil.service.ParticipanteService;

@RestController
@RequestMapping("/api/participantes")
@Tag(name = "Participantes", description = "Consulta de participantes registrados.")
public class ParticipanteController {

    private final ParticipanteService service;

    public ParticipanteController(ParticipanteService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Buscar participantes por DNI",
            description = "Busca coincidencias exactas por DNI y devuelve todos los campos de los registros encontrados, ordenados por id_participante.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados de la búsqueda obtenidos correctamente; puede ser una lista vacía",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Participante.class)))),
            @ApiResponse(responseCode = "400", description = "El parámetro dni es obligatorio"),
            @ApiResponse(responseCode = "500", description = "Error al consultar la base de datos")
    })
    public ResponseEntity<List<Participante>> buscarPorDni(
            @Parameter(description = "DNI exacto que se desea consultar.", required = true, example = "12345678")
            @RequestParam("dni") String dni) {
        if (dni.isBlank()) {
            throw new IllegalArgumentException("El parámetro dni no puede estar vacío.");
        }
        return ResponseEntity.ok(service.buscarPorDni(dni.trim()));
    }
}
