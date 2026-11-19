package pe.gob.devida.geodaismovil.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "RegistroParcela", description = "Datos de una parcela en la tabla public.registros_parcela")
public class RegistroParcelaDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Clave primaria única del registro, generada por el móvil", maxLength = 64, requiredMode = Schema.RequiredMode.REQUIRED, example = "id-local-del-registro")
    private String internalKey;

    @Schema(description = "Código asignado a la parcela", maxLength = 30, example = "PAR-0001")
    private String codigoAutogenerado;

    @Schema(description = "DNI del productor, exactamente 8 dígitos", minLength = 8, maxLength = 8, pattern = "^[0-9]{8}$", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345678")
    private String dniProductor;

    @Schema(description = "Número de parcela asignado por la base de datos para cada productor; no debe enviarse al crear el registro", maxLength = 50, example = "12345678_1", accessMode = Schema.AccessMode.READ_ONLY)
    private String nParcela;

    @Schema(description = "Nombres del productor", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED, example = "Ana")
    private String nombres;

    @Schema(description = "Apellido paterno del productor", maxLength = 100, example = "Perez")
    private String apellidoPaterno;

    @Schema(description = "Apellido materno del productor", maxLength = 100, example = "Garcia")
    private String apellidoMaterno;

    @Schema(description = "Nombre completo del productor", maxLength = 300, example = "Ana Perez Garcia")
    private String nombreCompleto;

    @Schema(description = "Fecha de nacimiento del productor", type = "string", format = "date", example = "1985-06-15")
    private LocalDate fechaNacimiento;

    @Schema(description = "Sexo del productor: M o F", allowableValues = {"M", "F"}, example = "F")
    private String sexo;

    @Schema(description = "Celular peruano del participante; debe comenzar en 9 y tener 9 dígitos", maxLength = 9, pattern = "^9[0-9]{8}$", example = "987654321")
    private String celularParticipante;

    @Schema(description = "Tipo de productor", maxLength = 100, example = "Agricultor")
    private String tipoProductor;

    @Schema(description = "Actividad agraria", maxLength = 150, example = "Agricultura")
    private String actividadAgraria;

    @Schema(description = "Tipo de cultivo", maxLength = 100, example = "Cafe")
    private String tipoCultivo;

    @Schema(description = "Variedad del cultivo", maxLength = 2, example = "01")
    private String variedad;

    @Schema(description = "Régimen de tenencia", maxLength = 100, example = "Propietario")
    private String regimenTenencia;

    @Schema(description = "Superficie registrada por MIDAGRI en hectáreas", example = "2.5")
    private BigDecimal superficieMidagri;

    @Schema(description = "Oficina zonal", maxLength = 100, example = "La Merced")
    private String oficinaZonal;

    @Schema(description = "Código ubigeo de seis dígitos", minLength = 6, maxLength = 6, pattern = "^[0-9]{6}$", example = "120101")
    private String ubigeo;

    @Schema(description = "Departamento", maxLength = 100, example = "Junin")
    private String departamento;

    @Schema(description = "Provincia", maxLength = 100, example = "Chanchamayo")
    private String provincia;

    @Schema(description = "Distrito", maxLength = 100, example = "Chanchamayo")
    private String distrito;

    @Schema(description = "Caserio", maxLength = 200, example = "Chanchamayo")
    private String caserio;

    @Schema(description = "Fuente del registro", maxLength = 30, example = "REA-MIDAGRI")
    private String fuente;

    @Schema(description = "Sistema de referencia; se usa WGS84 si se omite", maxLength = 20, defaultValue = "WGS84", example = "WGS84")
    private String datum;

    @Schema(description = "Observaciones", example = "Parcela registrada en campo")
    private String observaciones;

    @Schema(description = "DNI del profesional, obligatorio y de exactamente 8 dígitos", minLength = 8, maxLength = 8, pattern = "^[0-9]{8}$", requiredMode = Schema.RequiredMode.REQUIRED, example = "87654321")
    private String profesionalDni;

    @Schema(description = "Nombres del profesional", maxLength = 100, example = "Luis")
    private String profesionalNombres;

    @Schema(description = "Apellidos del profesional", maxLength = 150, example = "Gomez Ruiz")
    private String profesionalApellidos;

    @Schema(description = "Celular del profesional", maxLength = 9, pattern = "^9[0-9]{8}$", example = "912345678")
    private String profesionalCelular;

    @Schema(description = "Correo electrónico del profesional", maxLength = 255, example = "luis.gomez@devida.gob.pe")
    private String profesionalEmail;

    @Schema(description = "Ruta de la imagen anverso del DNI", example = "ruta/dni-anverso.jpg")
    private String rutaDniAnverso;

    @Schema(description = "Ruta de la imagen reverso del DNI", example = "ruta/dni-reverso.jpg")
    private String rutaDniReverso;

    @Schema(description = "Ruta de la foto 1", example = "ruta/foto1.jpg")
    private String rutaFoto1;

    @Schema(description = "Ruta de la foto 2", example = "ruta/foto2.jpg")
    private String rutaFoto2;

    @Schema(description = "Ruta de la foto 3", example = "ruta/foto3.jpg")
    private String rutaFoto3;

    @Schema(description = "Ruta de la foto 4", example = "ruta/foto4.jpg")
    private String rutaFoto4;

    @Schema(description = "Ruta de la foto 5", example = "ruta/foto5.jpg")
    private String rutaFoto5;

    @Schema(description = "Ruta de la foto 6", example = "ruta/foto6.jpg")
    private String rutaFoto6;

    @Schema(description = "Geometría obligatoria POINT, LINESTRING o POLYGON en WKT; el backend asigna SRID 4326. Admite 2D o 3D.", requiredMode = Schema.RequiredMode.REQUIRED, example = "POLYGON Z ((-77.0 -12.0 500, -76.9 -12.0 500, -76.9 -11.9 500, -77.0 -11.9 500, -77.0 -12.0 500))")
    private String geom;

    @Schema(description = "Centroide de la parcela en WKT POINT 2D SRID 4326", example = "POINT (-77.0 -12.0)")
    private String centroide;

    @Schema(description = "Área calculada en hectáreas", example = "1.25")
    private BigDecimal areaHa;

    @Schema(description = "Perímetro calculado en metros", example = "125.5")
    private BigDecimal perimetroM;

    @Schema(description = "Altitud en metros", example = "500.0")
    private BigDecimal altitud;

    @Schema(description = "Identificador único del dispositivo móvil", maxLength = 64, example = "device-uuid")
    private String deviceUuid;

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    @Schema(description = "Fecha y hora de captura registrada por el móvil", type = "string", format = "date-time", example = "2026-11-03T15:00:00-05:00")
    private OffsetDateTime fechaCaptura;

    @Schema(description = "Fecha y hora de creación; se asigna la hora actual si se omite", type = "string", format = "date-time", example = "2026-11-03T15:00:00-05:00")
    private OffsetDateTime fechaCreacion;

    @Schema(description = "Fecha y hora de última actualización; se asigna la hora actual si se omite", type = "string", format = "date-time", example = "2026-11-03T15:00:00-05:00")
    private OffsetDateTime fechaActualizacion;

    @Schema(description = "Indicador de borrado lógico; por defecto false", defaultValue = "false", example = "false")
    private Boolean eliminado;

    public String getInternalKey() {
        return internalKey;
    }

    public void setInternalKey(String internalKey) {
        this.internalKey = internalKey;
    }

    public String getCodigoAutogenerado() {
        return codigoAutogenerado;
    }

    public void setCodigoAutogenerado(String codigoAutogenerado) {
        this.codigoAutogenerado = codigoAutogenerado;
    }

    public String getDniProductor() {
        return dniProductor;
    }

    public void setDniProductor(String dniProductor) {
        this.dniProductor = dniProductor;
    }

    public String getNParcela() {
        return nParcela;
    }

    public void setNParcela(String nParcela) {
        this.nParcela = nParcela;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getCelularParticipante() {
        return celularParticipante;
    }

    public void setCelularParticipante(String celularParticipante) {
        this.celularParticipante = celularParticipante;
    }

    public String getTipoProductor() {
        return tipoProductor;
    }

    public void setTipoProductor(String tipoProductor) {
        this.tipoProductor = tipoProductor;
    }

    public String getActividadAgraria() {
        return actividadAgraria;
    }

    public void setActividadAgraria(String actividadAgraria) {
        this.actividadAgraria = actividadAgraria;
    }

    public String getTipoCultivo() {
        return tipoCultivo;
    }

    public void setTipoCultivo(String tipoCultivo) {
        this.tipoCultivo = tipoCultivo;
    }

    public String getVariedad() {
        return variedad;
    }

    public void setVariedad(String variedad) {
        this.variedad = variedad;
    }

    public String getRegimenTenencia() {
        return regimenTenencia;
    }

    public void setRegimenTenencia(String regimenTenencia) {
        this.regimenTenencia = regimenTenencia;
    }

    public BigDecimal getSuperficieMidagri() {
        return superficieMidagri;
    }

    public void setSuperficieMidagri(BigDecimal superficieMidagri) {
        this.superficieMidagri = superficieMidagri;
    }

    public String getOficinaZonal() {
        return oficinaZonal;
    }

    public void setOficinaZonal(String oficinaZonal) {
        this.oficinaZonal = oficinaZonal;
    }

    public String getUbigeo() {
        return ubigeo;
    }

    public void setUbigeo(String ubigeo) {
        this.ubigeo = ubigeo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getCaserio() {
        return caserio;
    }

    public void setCaserio(String caserio) {
        this.caserio = caserio;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getProfesionalDni() {
        return profesionalDni;
    }

    public void setProfesionalDni(String profesionalDni) {
        this.profesionalDni = profesionalDni;
    }

    public String getProfesionalNombres() {
        return profesionalNombres;
    }

    public void setProfesionalNombres(String profesionalNombres) {
        this.profesionalNombres = profesionalNombres;
    }

    public String getProfesionalApellidos() {
        return profesionalApellidos;
    }

    public void setProfesionalApellidos(String profesionalApellidos) {
        this.profesionalApellidos = profesionalApellidos;
    }

    public String getProfesionalCelular() {
        return profesionalCelular;
    }

    public void setProfesionalCelular(String profesionalCelular) {
        this.profesionalCelular = profesionalCelular;
    }

    public String getProfesionalEmail() {
        return profesionalEmail;
    }

    public void setProfesionalEmail(String profesionalEmail) {
        this.profesionalEmail = profesionalEmail;
    }

    public String getRutaDniAnverso() {
        return rutaDniAnverso;
    }

    public void setRutaDniAnverso(String rutaDniAnverso) {
        this.rutaDniAnverso = rutaDniAnverso;
    }

    public String getRutaDniReverso() {
        return rutaDniReverso;
    }

    public void setRutaDniReverso(String rutaDniReverso) {
        this.rutaDniReverso = rutaDniReverso;
    }

    public String getRutaFoto1() {
        return rutaFoto1;
    }

    public void setRutaFoto1(String rutaFoto1) {
        this.rutaFoto1 = rutaFoto1;
    }

    public String getRutaFoto2() {
        return rutaFoto2;
    }

    public void setRutaFoto2(String rutaFoto2) {
        this.rutaFoto2 = rutaFoto2;
    }

    public String getRutaFoto3() {
        return rutaFoto3;
    }

    public void setRutaFoto3(String rutaFoto3) {
        this.rutaFoto3 = rutaFoto3;
    }

    public String getRutaFoto4() {
        return rutaFoto4;
    }

    public void setRutaFoto4(String rutaFoto4) {
        this.rutaFoto4 = rutaFoto4;
    }

    public String getRutaFoto5() {
        return rutaFoto5;
    }

    public void setRutaFoto5(String rutaFoto5) {
        this.rutaFoto5 = rutaFoto5;
    }

    public String getRutaFoto6() {
        return rutaFoto6;
    }

    public void setRutaFoto6(String rutaFoto6) {
        this.rutaFoto6 = rutaFoto6;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getCentroide() {
        return centroide;
    }

    public void setCentroide(String centroide) {
        this.centroide = centroide;
    }

    public BigDecimal getAreaHa() {
        return areaHa;
    }

    public void setAreaHa(BigDecimal areaHa) {
        this.areaHa = areaHa;
    }

    public BigDecimal getPerimetroM() {
        return perimetroM;
    }

    public void setPerimetroM(BigDecimal perimetroM) {
        this.perimetroM = perimetroM;
    }

    public BigDecimal getAltitud() {
        return altitud;
    }

    public void setAltitud(BigDecimal altitud) {
        this.altitud = altitud;
    }

    public OffsetDateTime getFechaCaptura() {
        return fechaCaptura;
    }

    public void setFechaCaptura(OffsetDateTime fechaCaptura) {
        this.fechaCaptura = fechaCaptura;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }
}
