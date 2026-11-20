package pe.gob.devida.geodaismovil.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.devida.geodaismovil.model.Participante;
import pe.gob.devida.geodaismovil.repository.ParticipanteRepository;

@Service
public class ParticipanteService {

    private static final List<String> CSV_COLUMNS = List.of(
            "poa_pta",
            "actividad",
            "departamento",
            "provincia",
            "distrito",
            "nombre_lugar_intervencion",
            "categoria_lugar",
            "categoria_comunidad",
            "apellido_paterno",
            "apellido_materno",
            "nombres",
            "dni",
            "fecha_nacimiento",
            "sexo",
            "cultivo_asistido");

    private final ParticipanteRepository repository;

    public ParticipanteService(ParticipanteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Participante> buscarPorDni(String dni) {
        return repository.findByDniOrderByIdParticipanteAsc(dni);
    }

    @Transactional
    public int importarCsv(InputStream csv) throws IOException {
        if (csv == null) {
            throw new IllegalArgumentException("Debe proporcionar un archivo CSV.");
        }

        List<List<String>> rows;
        try {
            rows = parseCsv(csv);
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException("El archivo CSV debe estar codificado en UTF-8.", exception);
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV está vacío.");
        }

        List<String> headers = new ArrayList<>(rows.get(0));
        if (!headers.isEmpty() && headers.get(0).startsWith("\uFEFF")) {
            headers.set(0, headers.get(0).substring(1));
        }
        for (int i = 0; i < headers.size(); i++) {
            headers.set(i, headers.get(i).strip());
        }

        Map<String, Integer> columnIndexes = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            if (columnIndexes.putIfAbsent(headers.get(i), i) != null) {
                throw new IllegalArgumentException("La columna '" + headers.get(i) + "' está repetida en el encabezado.");
            }
        }
        Set<String> expectedColumns = Set.copyOf(CSV_COLUMNS);
        Set<String> actualColumns = columnIndexes.keySet();
        if (!actualColumns.equals(expectedColumns)) {
            List<String> missing = CSV_COLUMNS.stream().filter(column -> !actualColumns.contains(column)).toList();
            List<String> unexpected = actualColumns.stream().filter(column -> !expectedColumns.contains(column)).toList();
            throw new IllegalArgumentException(
                    "El encabezado debe contener las columnas de public.participante, excepto id_participante. "
                            + "Faltantes: " + missing + ". No reconocidas: " + unexpected + ".");
        }

        List<Participante> participantes = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            int csvLine = rowIndex + 1;
            if (row.size() != headers.size()) {
                throw new IllegalArgumentException("La fila " + csvLine + " tiene " + row.size()
                        + " valores; se esperaban " + headers.size() + ".");
            }
            participantes.add(toParticipante(row, columnIndexes, csvLine));
        }
        if (participantes.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV no contiene registros para importar.");
        }

        repository.saveAll(participantes);
        return participantes.size();
    }

    private Participante toParticipante(List<String> row, Map<String, Integer> columnIndexes, int csvLine) {
        Participante participante = new Participante();
        participante.setPoaPta(value(row, columnIndexes, "poa_pta", 100, csvLine));
        participante.setActividad(value(row, columnIndexes, "actividad", 255, csvLine));
        participante.setDepartamento(value(row, columnIndexes, "departamento", 255, csvLine));
        participante.setProvincia(value(row, columnIndexes, "provincia", 255, csvLine));
        participante.setDistrito(value(row, columnIndexes, "distrito", 255, csvLine));
        participante.setNombreLugarIntervencion(value(row, columnIndexes, "nombre_lugar_intervencion", 255, csvLine));
        participante.setCategoriaLugar(value(row, columnIndexes, "categoria_lugar", 255, csvLine));
        participante.setCategoriaComunidad(value(row, columnIndexes, "categoria_comunidad", 255, csvLine));
        participante.setApellidoPaterno(value(row, columnIndexes, "apellido_paterno", 255, csvLine));
        participante.setApellidoMaterno(value(row, columnIndexes, "apellido_materno", 255, csvLine));
        participante.setNombres(value(row, columnIndexes, "nombres", 255, csvLine));
        participante.setDni(value(row, columnIndexes, "dni", 20, csvLine));
        participante.setFechaNacimiento(dateValue(row, columnIndexes, csvLine));
        participante.setSexo(value(row, columnIndexes, "sexo", 20, csvLine));
        participante.setCultivoAsistido(value(row, columnIndexes, "cultivo_asistido", 150, csvLine));
        return participante;
    }

    private String value(List<String> row, Map<String, Integer> columnIndexes, String column, int maxLength, int csvLine) {
        String value = row.get(columnIndexes.get(column)).strip();
        if (value.isEmpty()) {
            return null;
        }
        if (value.codePointCount(0, value.length()) > maxLength) {
            throw new IllegalArgumentException("El valor de '" + column + "' en la fila " + csvLine
                    + " supera el máximo de " + maxLength + " caracteres.");
        }
        return value;
    }

    private LocalDate dateValue(List<String> row, Map<String, Integer> columnIndexes, int csvLine) {
        String value = row.get(columnIndexes.get("fecha_nacimiento")).strip();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La fecha_nacimiento de la fila " + csvLine
                    + " debe tener formato yyyy-MM-dd.", exception);
        }
    }

    private List<List<String>> parseCsv(InputStream csv) throws IOException {
        var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try (PushbackReader reader = new PushbackReader(new BufferedReader(new InputStreamReader(csv, decoder)), 1)) {
            List<List<String>> rows = new ArrayList<>();
            List<String> row = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean inQuotes = false;
            boolean afterQuote = false;
            boolean rowStarted = false;
            int current;

            while ((current = reader.read()) != -1) {
                char character = (char) current;
                if (inQuotes) {
                    if (character == '"') {
                        int next = reader.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            inQuotes = false;
                            afterQuote = true;
                            if (next != -1) {
                                reader.unread(next);
                            }
                        }
                    } else {
                        field.append(character);
                    }
                    continue;
                }

                if (afterQuote && character != ',' && character != '\r' && character != '\n') {
                    throw new IllegalArgumentException("El CSV contiene caracteres inesperados después de un campo entre comillas.");
                }
                afterQuote = false;

                if (character == '"') {
                    if (field.length() != 0) {
                        throw new IllegalArgumentException("El CSV contiene comillas en una posición inválida.");
                    }
                    inQuotes = true;
                    rowStarted = true;
                } else if (character == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                } else if (character == '\r' || character == '\n') {
                    if (character == '\r') {
                        int next = reader.read();
                        if (next != '\n' && next != -1) {
                            reader.unread(next);
                        }
                    }
                    addCsvRow(rows, row, field);
                    row = new ArrayList<>();
                    field = new StringBuilder();
                    rowStarted = false;
                } else {
                    field.append(character);
                    rowStarted = true;
                }
            }

            if (inQuotes) {
                throw new IllegalArgumentException("El CSV contiene un campo entre comillas sin cerrar.");
            }
            if (rowStarted || !row.isEmpty() || field.length() > 0 || afterQuote) {
                addCsvRow(rows, row, field);
            }
            return rows;
        }
    }

    private void addCsvRow(List<List<String>> rows, List<String> row, StringBuilder field) {
        row.add(field.toString());
        if (row.size() != 1 || !row.get(0).isBlank()) {
            rows.add(List.copyOf(row));
        }
    }
}
