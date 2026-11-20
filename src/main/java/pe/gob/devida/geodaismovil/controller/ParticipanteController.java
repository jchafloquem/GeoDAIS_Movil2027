package pe.gob.devida.geodaismovil.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.gob.devida.geodaismovil.model.Participante;
import pe.gob.devida.geodaismovil.service.ParticipanteService;

@RestController
@RequestMapping("/api/participantes")
@Tag(name = "Participantes", description = "Consulta y carga CSV de participantes registrados.")
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

    @PostMapping(path = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar participantes desde un CSV",
            description = "Sube un CSV UTF-8 separado por comas. La primera fila debe incluir, en cualquier orden, las columnas de public.participante excepto id_participante (autogenerado). fecha_nacimiento debe usar yyyy-MM-dd; las celdas vacías se guardan como null. La importación es atómica: ante cualquier fila inválida no se guarda ningún registro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registros importados correctamente",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"success\":true,\"importedRows\":2}"))),
            @ApiResponse(responseCode = "400", description = "Archivo vacío, encabezado inválido o fila incompatible con la estructura de public.participante"),
            @ApiResponse(responseCode = "500", description = "Error al leer el archivo o guardar los registros")
    })
    public ResponseEntity<Map<String, Object>> importarCsv(
            @Parameter(description = "Archivo CSV UTF-8. Encabezados: poa_pta, actividad, departamento, provincia, distrito, nombre_lugar_intervencion, categoria_lugar, categoria_comunidad, apellido_paterno, apellido_materno, nombres, dni, fecha_nacimiento, sexo, cultivo_asistido.",
                    required = true, schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV está vacío.");
        }
        int importedRows = service.importarCsv(file.getInputStream());
        return ResponseEntity.ok(Map.of("success", true, "importedRows", importedRows));
    }
}
