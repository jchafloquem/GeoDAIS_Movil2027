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
}
