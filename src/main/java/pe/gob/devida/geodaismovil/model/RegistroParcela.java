package pe.gob.devida.geodaismovil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Entidad JPA mapeada a la tabla {@code public.registros_parcela} de PostgreSQL
 * con PostGIS.
 *
 * <p>
 * Los campos {@code geom} y {@code centroide} se mapean con tipos JTS de
 * Hibernate Spatial para persistir las geometrías PostGIS.
 *
 * <p>
 * Los campos de imágenes ({@code rutaDniAnverso}, {@code rutaDniReverso},
 * {@code rutaFoto1} a {@code rutaFoto6}) son rutas de texto que apuntan a un
 * repositorio físico en el servidor; <b>no se aplican validaciones de URL/HTTPS
 * en el esquema</b>, ya que el cliente puede proporcionar rutas relativas o
 * nombres de archivo.
 */
@Entity
@Table(name = "registros_parcela", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroParcela implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Clave primaria única. Generada por el móvil (trabajo offline). */
    @Id
    @Column(name = "internal_key", nullable = false, length = 64)
    private String internalKey;

    @Column(name = "codigo_autogenerado", length = 30)
    private String codigoAutogenerado;

    /**
     * DNI obligatorio; la combinación dni_productor + n_parcela es única.
     */
    @Column(name = "dni_productor", nullable = false, length = 8, columnDefinition = "char(8)")
    private String dniProductor;

    @Column(name = "n_parcela", length = 50)
    private String nParcela;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;

    @Column(name = "nombre_completo", length = 300)
    private String nombreCompleto;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "sexo", length = 1, columnDefinition = "char(1)")
    private String sexo;

    @Column(name = "celular_participante", length = 9)
    private String celularParticipante;

    @Column(name = "tipo_productor", length = 100)
    private String tipoProductor;

    @Column(name = "actividad_agraria", length = 150)
    private String actividadAgraria;

    @Column(name = "tipo_cultivo", length = 100)
    private String tipoCultivo;

    @Column(name = "variedad", length = 2)
    private String variedad;

    @Column(name = "regimen_tenencia", length = 100)
    private String regimenTenencia;

    @Column(name = "superficie_midagri", precision = 10, scale = 4)
    private BigDecimal superficieMidagri;

    @Column(name = "oficina_zonal", length = 100)
    private String oficinaZonal;

    @Column(name = "ubigeo", length = 6, columnDefinition = "char(6)")
    private String ubigeo;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "distrito", length = 100)
    private String distrito;

    @Column(name = "caserio", length = 200)
    private String caserio;

    @Column(name = "fuente", length = 30)
    private String fuente;

    @Column(name = "datum", nullable = false, length = 20,
            columnDefinition = "varchar(20) default 'WGS84'")
    @Builder.Default
    private String datum = "WGS84";

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "profesional_dni", nullable = false, length = 8, columnDefinition = "char(8)")
    private String profesionalDni;

    @Column(name = "profesional_nombres", length = 100)
    private String profesionalNombres;

    @Column(name = "profesional_apellidos", length = 150)
    private String profesionalApellidos;

    @Column(name = "profesional_celular", length = 9)
    private String profesionalCelular;

    @Column(name = "profesional_email", length = 255)
    private String profesionalEmail;

    /** Ruta de la imagen anverso del DNI. No se valida HTTPS aquí. */
    @Column(name = "ruta_dni_anverso", columnDefinition = "text")
    private String rutaDniAnverso;

    /** Ruta de la imagen reverso del DNI. No se valida HTTPS aquí. */
    @Column(name = "ruta_dni_reverso", columnDefinition = "text")
    private String rutaDniReverso;

    /** Ruta de la foto 1. */
    @Column(name = "ruta_foto1", columnDefinition = "text")
    private String rutaFoto1;

    /** Ruta de la foto 2. */
    @Column(name = "ruta_foto2", columnDefinition = "text")
    private String rutaFoto2;

    /** Ruta de la foto 3. */
    @Column(name = "ruta_foto3", columnDefinition = "text")
    private String rutaFoto3;

    /** Ruta de la foto 4. */
    @Column(name = "ruta_foto4", columnDefinition = "text")
    private String rutaFoto4;

    /** Ruta de la foto 5. */
    @Column(name = "ruta_foto5", columnDefinition = "text")
    private String rutaFoto5;

    /** Ruta de la foto 6. */
    @Column(name = "ruta_foto6", columnDefinition = "text")
    private String rutaFoto6;

    /**
     * Geometría de parcela en SRID 4326. La tabla valida tipo, dimensión y
     * validez mediante sus restricciones PostGIS.
     */
    @Column(name = "geom", nullable = false, columnDefinition = "geometry")
    private Geometry geom;

    /**
     * Centroide de la parcela en SRID 4326.
     * Column definition: geometry(Point, 4326)
     */
    @Column(name = "centroide", columnDefinition = "geometry(Point, 4326)")
    private Point centroide;

    @Column(name = "area_ha", precision = 12, scale = 6)
    private BigDecimal areaHa;

    @Column(name = "perimetro_m", precision = 12, scale = 2)
    private BigDecimal perimetroM;

    @Column(name = "altitud", precision = 8, scale = 2)
    private BigDecimal altitud;

    @Column(name = "device_uuid", length = 64)
    private String deviceUuid;

    @Column(name = "fecha_captura")
    private OffsetDateTime fechaCaptura;

    /** Fecha/hora de creación. Obligatoria en la tabla de destino. */
    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "timestamptz default now()")
    private OffsetDateTime fechaCreacion;

    /** Fecha/hora de última actualización. Obligatoria en la tabla de destino. */
    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "timestamptz default now()")
    private OffsetDateTime fechaActualizacion;

    /** Borrado lógico. Obligatorio en la tabla de destino. */
    @Column(name = "eliminado", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean eliminado = false;

    public String getInternalKey() {
        return internalKey;
    }

    public void setInternalKey(String internalKey) {
        this.internalKey = internalKey;
    }

    @PrePersist
    private void setDefaultsBeforeInsert() {
        OffsetDateTime now = OffsetDateTime.now();
        if (datum == null) {
            datum = "WGS84";
        }
        if (fechaCreacion == null) {
            fechaCreacion = now;
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = now;
        }
        if (eliminado == null) {
            eliminado = false;
        }
    }

    @PreUpdate
    private void updateTimestamp() {
        fechaActualizacion = OffsetDateTime.now();
    }

    public void setCodigoAutogenerado(String codigoAutogenerado) {
        this.codigoAutogenerado = codigoAutogenerado;
    }

    public void setDniProductor(String dniProductor) {
        this.dniProductor = dniProductor;
    }

    public void setNParcela(String nParcela) {
        this.nParcela = nParcela;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public void setCelularParticipante(String celularParticipante) {
        this.celularParticipante = celularParticipante;
    }

    public void setTipoProductor(String tipoProductor) {
        this.tipoProductor = tipoProductor;
    }

    public void setActividadAgraria(String actividadAgraria) {
        this.actividadAgraria = actividadAgraria;
    }

    public void setTipoCultivo(String tipoCultivo) {
        this.tipoCultivo = tipoCultivo;
    }

    public void setVariedad(String variedad) {
        this.variedad = variedad;
    }

    public void setRegimenTenencia(String regimenTenencia) {
        this.regimenTenencia = regimenTenencia;
    }

    public void setSuperficieMidagri(BigDecimal superficieMidagri) {
        this.superficieMidagri = superficieMidagri;
    }

    public void setOficinaZonal(String oficinaZonal) {
        this.oficinaZonal = oficinaZonal;
    }

    public void setUbigeo(String ubigeo) {
        this.ubigeo = ubigeo;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public void setCaserio(String caserio) {
        this.caserio = caserio;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public void setProfesionalDni(String profesionalDni) {
        this.profesionalDni = profesionalDni;
    }

    public void setProfesionalNombres(String profesionalNombres) {
        this.profesionalNombres = profesionalNombres;
    }

    public void setProfesionalApellidos(String profesionalApellidos) {
        this.profesionalApellidos = profesionalApellidos;
    }

    public void setProfesionalCelular(String profesionalCelular) {
        this.profesionalCelular = profesionalCelular;
    }

    public void setProfesionalEmail(String profesionalEmail) {
        this.profesionalEmail = profesionalEmail;
    }

    public void setRutaDniAnverso(String rutaDniAnverso) {
        this.rutaDniAnverso = rutaDniAnverso;
    }

    public void setRutaDniReverso(String rutaDniReverso) {
        this.rutaDniReverso = rutaDniReverso;
    }

    public void setRutaFoto1(String rutaFoto1) {
        this.rutaFoto1 = rutaFoto1;
    }

    public void setRutaFoto2(String rutaFoto2) {
        this.rutaFoto2 = rutaFoto2;
    }

    public void setRutaFoto3(String rutaFoto3) {
        this.rutaFoto3 = rutaFoto3;
    }

    public void setRutaFoto4(String rutaFoto4) {
        this.rutaFoto4 = rutaFoto4;
    }

    public void setRutaFoto5(String rutaFoto5) {
        this.rutaFoto5 = rutaFoto5;
    }

    public void setRutaFoto6(String rutaFoto6) {
        this.rutaFoto6 = rutaFoto6;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public void setCentroide(Point centroide) {
        this.centroide = centroide;
    }

    public void setAreaHa(BigDecimal areaHa) {
        this.areaHa = areaHa;
    }

    public void setPerimetroM(BigDecimal perimetroM) {
        this.perimetroM = perimetroM;
    }

    public void setAltitud(BigDecimal altitud) {
        this.altitud = altitud;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public void setFechaCaptura(OffsetDateTime fechaCaptura) {
        this.fechaCaptura = fechaCaptura;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public String getCodigoAutogenerado() {
        return codigoAutogenerado;
    }

    public String getDniProductor() {
        return dniProductor;
    }

    public String getNParcela() {
        return nParcela;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public String getCelularParticipante() {
        return celularParticipante;
    }

    public String getTipoProductor() {
        return tipoProductor;
    }

    public String getActividadAgraria() {
        return actividadAgraria;
    }

    public String getTipoCultivo() {
        return tipoCultivo;
    }

    public String getVariedad() {
        return variedad;
    }

    public String getRegimenTenencia() {
        return regimenTenencia;
    }

    public BigDecimal getSuperficieMidagri() {
        return superficieMidagri;
    }

    public String getOficinaZonal() {
        return oficinaZonal;
    }

    public String getUbigeo() {
        return ubigeo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getDistrito() {
        return distrito;
    }

    public String getCaserio() {
        return caserio;
    }

    public String getFuente() {
        return fuente;
    }

    public String getDatum() {
        return datum;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public String getProfesionalDni() {
        return profesionalDni;
    }

    public String getProfesionalNombres() {
        return profesionalNombres;
    }

    public String getProfesionalApellidos() {
        return profesionalApellidos;
    }

    public String getProfesionalCelular() {
        return profesionalCelular;
    }

    public String getProfesionalEmail() {
        return profesionalEmail;
    }

    public String getRutaDniAnverso() {
        return rutaDniAnverso;
    }

    public String getRutaDniReverso() {
        return rutaDniReverso;
    }

    public String getRutaFoto1() {
        return rutaFoto1;
    }

    public String getRutaFoto2() {
        return rutaFoto2;
    }

    public String getRutaFoto3() {
        return rutaFoto3;
    }

    public String getRutaFoto4() {
        return rutaFoto4;
    }

    public String getRutaFoto5() {
        return rutaFoto5;
    }

    public String getRutaFoto6() {
        return rutaFoto6;
    }

    public Geometry getGeom() {
        return geom;
    }

    public Point getCentroide() {
        return centroide;
    }

    public BigDecimal getAreaHa() {
        return areaHa;
    }

    public BigDecimal getPerimetroM() {
        return perimetroM;
    }

    public BigDecimal getAltitud() {
        return altitud;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public OffsetDateTime getFechaCaptura() {
        return fechaCaptura;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
