package pe.gob.devida.geodaismovil.controller;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import pe.gob.devida.geodaismovil.dto.RegistroParcelaDto;
import pe.gob.devida.geodaismovil.model.RegistroParcela;
import pe.gob.devida.geodaismovil.repository.RegistroParcelaRepository;

@RestController
@RequestMapping("/api/parcelas")
@Tag(name = "Parcelas", description = "Registro, consulta paginada y borrado lógico de parcelas.")
public class RegistroParcelaController {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final RegistroParcelaRepository repository;

    public RegistroParcelaController(RegistroParcelaRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Operation(
            summary = "Crear o actualizar un registro de parcela",
            description = "Guarda la parcela usando internalKey como clave. La geometría geom debe enviarse como WKT POINT, LINESTRING o POLYGON; el backend asigna SRID 4326. Las geometrías deben cumplir las restricciones PostGIS de validez y dimensión.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro guardado correctamente",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"success\":true,\"id\":\"id-local-del-registro\"}"))),
            @ApiResponse(responseCode = "400", description = "El cuerpo, la geometría WKT o los datos no cumplen las restricciones de la tabla"),
            @ApiResponse(responseCode = "500", description = "Error al guardar el registro")
    })
    public ResponseEntity<Map<String, Object>> crearRegistro(@RequestBody RegistroParcelaDto dto) {
        RegistroParcela entidad = mapDtoToEntity(dto);
        RegistroParcela guardada = repository.save(entidad);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("id", guardada.getInternalKey());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar parcelas activas",
            description = "Devuelve una página de registros con eliminado=false. page empieza en 0; size admite valores de 1 a 100.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de parcelas obtenida",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"success\":true,\"data\":[],\"page\":0,\"size\":50,\"total\":0,\"totalPages\":0}"))),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación fuera de rango"),
            @ApiResponse(responseCode = "500", description = "Error al consultar la base de datos")
    })
    public ResponseEntity<Map<String, Object>> listar(
            @Parameter(description = "Número de página, comenzando en 0.", example = "0",
                    schema = @Schema(minimum = "0", defaultValue = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de registros por página; máximo 100.", example = "50",
                    schema = @Schema(minimum = "1", maximum = "100", defaultValue = "50"))
            @RequestParam(defaultValue = "50") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("La paginación debe tener page >= 0 y size entre 1 y 100.");
        }

        Page<RegistroParcela> result = repository.findAllByEliminadoFalse(PageRequest.of(page, size));
        List<RegistroParcelaDto> data = result.getContent().stream()
                .map(this::mapEntityToDto)
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("total", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{internalKey}")
    @Operation(
            summary = "Obtener una parcela por clave",
            description = "Devuelve el registro si existe y no está marcado como eliminado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcela encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegistroParcelaDto.class))),
            @ApiResponse(responseCode = "404", description = "No existe una parcela activa con esa clave"),
            @ApiResponse(responseCode = "500", description = "Error al consultar la base de datos")
    })
    public ResponseEntity<RegistroParcelaDto> obtener(
            @Parameter(description = "Clave primaria internal_key de la parcela.", example = "id-local-del-registro")
            @PathVariable String internalKey) {
        return repository.findByInternalKeyAndEliminadoFalse(internalKey)
                .map(registro -> ResponseEntity.ok(mapEntityToDto(registro)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{internalKey}")
    @Operation(
            summary = "Eliminar una parcela lógicamente",
            description = "Marca eliminado=true y actualiza fecha_actualizacion. No borra físicamente la fila; los registros eliminados dejan de aparecer en las consultas GET.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Parcela marcada como eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe una parcela con esa clave"),
            @ApiResponse(responseCode = "500", description = "Error al actualizar la parcela")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Clave primaria internal_key de la parcela.", example = "id-local-del-registro")
            @PathVariable String internalKey) {
        int actualizados = repository.marcarEliminado(internalKey, OffsetDateTime.now());
        return actualizados == 0 ? ResponseEntity.notFound().build() : ResponseEntity.noContent().build();
    }

    private RegistroParcela mapDtoToEntity(RegistroParcelaDto dto) {
        RegistroParcela entidad = new RegistroParcela();
        entidad.setInternalKey(dto.getInternalKey());
        entidad.setCodigoAutogenerado(dto.getCodigoAutogenerado());
        entidad.setDniProductor(dto.getDniProductor());
        entidad.setNombres(dto.getNombres());
        entidad.setApellidoPaterno(dto.getApellidoPaterno());
        entidad.setApellidoMaterno(dto.getApellidoMaterno());
        entidad.setNombreCompleto(dto.getNombreCompleto());
        entidad.setFechaNacimiento(dto.getFechaNacimiento());
        entidad.setSexo(dto.getSexo());
        entidad.setCelularParticipante(dto.getCelularParticipante());
        entidad.setTipoProductor(dto.getTipoProductor());
        entidad.setActividadAgraria(dto.getActividadAgraria());
        entidad.setTipoCultivo(dto.getTipoCultivo());
        entidad.setVariedad(dto.getVariedad());
        entidad.setRegimenTenencia(dto.getRegimenTenencia());
        entidad.setSuperficieMidagri(dto.getSuperficieMidagri());
        entidad.setOficinaZonal(dto.getOficinaZonal());
        entidad.setUbigeo(dto.getUbigeo());
        entidad.setDepartamento(dto.getDepartamento());
        entidad.setProvincia(dto.getProvincia());
        entidad.setDistrito(dto.getDistrito());
        entidad.setCaserio(dto.getCaserio());
        entidad.setFuente(dto.getFuente());
        entidad.setDatum(dto.getDatum());
        entidad.setObservaciones(dto.getObservaciones());
        entidad.setProfesionalDni(dto.getProfesionalDni());
        entidad.setProfesionalNombres(dto.getProfesionalNombres());
        entidad.setProfesionalApellidos(dto.getProfesionalApellidos());
        entidad.setProfesionalCelular(dto.getProfesionalCelular());
        entidad.setProfesionalEmail(dto.getProfesionalEmail());
        entidad.setRutaDniAnverso(dto.getRutaDniAnverso());
        entidad.setRutaDniReverso(dto.getRutaDniReverso());
        entidad.setRutaFoto1(dto.getRutaFoto1());
        entidad.setRutaFoto2(dto.getRutaFoto2());
        entidad.setRutaFoto3(dto.getRutaFoto3());
        entidad.setRutaFoto4(dto.getRutaFoto4());
        entidad.setRutaFoto5(dto.getRutaFoto5());
        entidad.setRutaFoto6(dto.getRutaFoto6());
        entidad.setGeom(parseGeometry(dto.getGeom()));
        entidad.setCentroide(parsePoint(dto.getCentroide()));
        entidad.setAreaHa(dto.getAreaHa());
        entidad.setPerimetroM(dto.getPerimetroM());
        entidad.setAltitud(dto.getAltitud());
        entidad.setDeviceUuid(dto.getDeviceUuid());
        entidad.setFechaCaptura(dto.getFechaCaptura());
        entidad.setFechaCreacion(dto.getFechaCreacion());
        entidad.setFechaActualizacion(dto.getFechaActualizacion());
        entidad.setEliminado(dto.getEliminado());
        return entidad;
    }

    private RegistroParcelaDto mapEntityToDto(RegistroParcela entidad) {
        RegistroParcelaDto dto = new RegistroParcelaDto();
        dto.setInternalKey(entidad.getInternalKey());
        dto.setCodigoAutogenerado(entidad.getCodigoAutogenerado());
        dto.setDniProductor(entidad.getDniProductor());
        dto.setNParcela(entidad.getNParcela());
        dto.setNombres(entidad.getNombres());
        dto.setApellidoPaterno(entidad.getApellidoPaterno());
        dto.setApellidoMaterno(entidad.getApellidoMaterno());
        dto.setNombreCompleto(entidad.getNombreCompleto());
        dto.setFechaNacimiento(entidad.getFechaNacimiento());
        dto.setSexo(entidad.getSexo());
        dto.setCelularParticipante(entidad.getCelularParticipante());
        dto.setTipoProductor(entidad.getTipoProductor());
        dto.setActividadAgraria(entidad.getActividadAgraria());
        dto.setTipoCultivo(entidad.getTipoCultivo());
        dto.setVariedad(entidad.getVariedad());
        dto.setRegimenTenencia(entidad.getRegimenTenencia());
        dto.setSuperficieMidagri(entidad.getSuperficieMidagri());
        dto.setOficinaZonal(entidad.getOficinaZonal());
        dto.setUbigeo(entidad.getUbigeo());
        dto.setDepartamento(entidad.getDepartamento());
        dto.setProvincia(entidad.getProvincia());
        dto.setDistrito(entidad.getDistrito());
        dto.setCaserio(entidad.getCaserio());
        dto.setFuente(entidad.getFuente());
        dto.setDatum(entidad.getDatum());
        dto.setObservaciones(entidad.getObservaciones());
        dto.setProfesionalDni(entidad.getProfesionalDni());
        dto.setProfesionalNombres(entidad.getProfesionalNombres());
        dto.setProfesionalApellidos(entidad.getProfesionalApellidos());
        dto.setProfesionalCelular(entidad.getProfesionalCelular());
        dto.setProfesionalEmail(entidad.getProfesionalEmail());
        dto.setRutaDniAnverso(entidad.getRutaDniAnverso());
        dto.setRutaDniReverso(entidad.getRutaDniReverso());
        dto.setRutaFoto1(entidad.getRutaFoto1());
        dto.setRutaFoto2(entidad.getRutaFoto2());
        dto.setRutaFoto3(entidad.getRutaFoto3());
        dto.setRutaFoto4(entidad.getRutaFoto4());
        dto.setRutaFoto5(entidad.getRutaFoto5());
        dto.setRutaFoto6(entidad.getRutaFoto6());
        dto.setGeom(toWkt(entidad.getGeom()));
        dto.setCentroide(toWkt(entidad.getCentroide()));
        dto.setAreaHa(entidad.getAreaHa());
        dto.setPerimetroM(entidad.getPerimetroM());
        dto.setAltitud(entidad.getAltitud());
        dto.setDeviceUuid(entidad.getDeviceUuid());
        dto.setFechaCaptura(entidad.getFechaCaptura());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setFechaActualizacion(entidad.getFechaActualizacion());
        dto.setEliminado(entidad.getEliminado());
        return dto;
    }

    private Geometry parseGeometry(String wkt) {
        if (wkt == null || wkt.isBlank()) {
            throw new IllegalArgumentException("La geometría de parcela es obligatoria.");
        }
        try {
            Geometry geometry = new WKTReader(GEOMETRY_FACTORY).read(wkt);
            geometry.setSRID(4326);
            String type = geometry.getGeometryType();
            if (!type.equals("Point") && !type.equals("LineString") && !type.equals("Polygon")) {
                throw new IllegalArgumentException("La geometría debe ser POINT, LINESTRING o POLYGON.");
            }
            int dimension = coordinateDimension(geometry);
            if (dimension != 2 && dimension != 3) {
                throw new IllegalArgumentException("La geometría debe tener 2 o 3 dimensiones.");
            }
            if (!geometry.isValid()) {
                throw new IllegalArgumentException("La geometría de parcela no es válida.");
            }
            return geometry;
        } catch (ParseException exception) {
            throw new IllegalArgumentException("La geometría de parcela debe tener formato WKT válido.", exception);
        }
    }

    private Point parsePoint(String wkt) {
        if (wkt == null || wkt.isBlank()) {
            return null;
        }
        Geometry geometry;
        try {
            geometry = new WKTReader(GEOMETRY_FACTORY).read(wkt);
        } catch (ParseException exception) {
            throw new IllegalArgumentException("El centroide debe tener formato WKT POINT válido.", exception);
        }
        if (!(geometry instanceof Point point)) {
            throw new IllegalArgumentException("El centroide debe ser de tipo POINT.");
        }
        if (coordinateDimension(point.getCoordinateSequence()) != 2) {
            throw new IllegalArgumentException("El centroide debe tener 2 dimensiones.");
        }
        point.setSRID(4326);
        return point;
    }

    private int coordinateDimension(Geometry geometry) {
        if (geometry instanceof Point point) {
            return coordinateDimension(point.getCoordinateSequence());
        }
        if (geometry instanceof LineString lineString) {
            return coordinateDimension(lineString.getCoordinateSequence());
        }
        if (geometry instanceof Polygon polygon) {
            return coordinateDimension(polygon.getExteriorRing().getCoordinateSequence());
        }
        throw new IllegalArgumentException("El tipo de geometría no está permitido.");
    }

    private int coordinateDimension(CoordinateSequence sequence) {
        int measures = sequence.getMeasures();
        int spatialDimension = sequence.getDimension() - measures;
        if (spatialDimension == 3) {
            boolean hasZ = false;
            for (int i = 0; i < sequence.size(); i++) {
                if (!Double.isNaN(sequence.getZ(i))) {
                    hasZ = true;
                    break;
                }
            }
            return (hasZ ? 3 : 2) + measures;
        }
        return sequence.getDimension();
    }

    private String toWkt(Geometry geometry) {
        return geometry == null ? null : new WKTWriter().write(geometry);
    }
}
