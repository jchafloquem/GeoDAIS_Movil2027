package pe.gob.devida.geodaismovil.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "participante", schema = "public")
@Schema(name = "Participante", description = "Registro de participante de public.participante.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_participante", nullable = false)
    @Schema(description = "Identificador único del participante.", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer idParticipante;

    @Column(name = "poa_pta", length = 100)
    @Schema(description = "POA o PTA asociado.", maxLength = 100, example = "POA 2026")
    private String poaPta;

    @Column(name = "actividad", length = 255)
    @Schema(description = "Actividad registrada.", maxLength = 255, example = "Asistencia técnica")
    private String actividad;

    @Column(name = "departamento", length = 255)
    @Schema(description = "Departamento.", maxLength = 255, example = "Junín")
    private String departamento;

    @Column(name = "provincia", length = 255)
    @Schema(description = "Provincia.", maxLength = 255, example = "Chanchamayo")
    private String provincia;

    @Column(name = "distrito", length = 255)
    @Schema(description = "Distrito.", maxLength = 255, example = "Pichanaqui")
    private String distrito;

    @Column(name = "nombre_lugar_intervencion", length = 255)
    @Schema(description = "Nombre del lugar de intervención.", maxLength = 255, example = "Centro poblado")
    private String nombreLugarIntervencion;

    @Column(name = "categoria_lugar", length = 255)
    @Schema(description = "Categoría del lugar.", maxLength = 255, example = "Comunidad")
    private String categoriaLugar;

    @Column(name = "categoria_comunidad", length = 255)
    @Schema(description = "Categoría de la comunidad.", maxLength = 255, example = "Nativa")
    private String categoriaComunidad;

    @Column(name = "apellido_paterno", length = 255)
    @Schema(description = "Apellido paterno.", maxLength = 255, example = "Pérez")
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 255)
    @Schema(description = "Apellido materno.", maxLength = 255, example = "García")
    private String apellidoMaterno;

    @Column(name = "nombres", length = 255)
    @Schema(description = "Nombres.", maxLength = 255, example = "Ana María")
    private String nombres;

    @Column(name = "dni", length = 20)
    @Schema(description = "Documento de identidad.", maxLength = 20, example = "12345678")
    private String dni;

    @Column(name = "fecha_nacimiento")
    @Schema(description = "Fecha de nacimiento.", type = "string", format = "date", example = "1985-06-15")
    private LocalDate fechaNacimiento;

    @Column(name = "sexo", length = 20)
    @Schema(description = "Sexo.", maxLength = 20, example = "F")
    private String sexo;

    @Column(name = "cultivo_asistido", length = 150)
    @Schema(description = "Cultivo asistido.", maxLength = 150, example = "Café")
    private String cultivoAsistido;

    public Integer getIdParticipante() {
        return idParticipante;
    }

    public void setIdParticipante(Integer idParticipante) {
        this.idParticipante = idParticipante;
    }

    public String getPoaPta() {
        return poaPta;
    }

    public void setPoaPta(String poaPta) {
        this.poaPta = poaPta;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
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

    public String getNombreLugarIntervencion() {
        return nombreLugarIntervencion;
    }

    public void setNombreLugarIntervencion(String nombreLugarIntervencion) {
        this.nombreLugarIntervencion = nombreLugarIntervencion;
    }

    public String getCategoriaLugar() {
        return categoriaLugar;
    }

    public void setCategoriaLugar(String categoriaLugar) {
        this.categoriaLugar = categoriaLugar;
    }

    public String getCategoriaComunidad() {
        return categoriaComunidad;
    }

    public void setCategoriaComunidad(String categoriaComunidad) {
        this.categoriaComunidad = categoriaComunidad;
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

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public String getCultivoAsistido() {
        return cultivoAsistido;
    }

    public void setCultivoAsistido(String cultivoAsistido) {
        this.cultivoAsistido = cultivoAsistido;
    }
}
