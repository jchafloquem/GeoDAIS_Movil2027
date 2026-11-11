package pe.gob.devida.geodaismovil.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegistroParcelaRequest", description = "Datos del registro de parcela sincronizados desde el móvil")
public class RegistroParcelaRequest {

	@Schema(description = "Clave primaria única del registro, generada por el móvil; permite trabajar offline. Si ya existe, el registro se actualiza.", maxLength = 64, requiredMode = Schema.RequiredMode.REQUIRED, example = "id-local-del-registro")
	public String internal_key;

	@Schema(description = "Código asignado a la parcela; opcional y puede repetirse.", maxLength = 30, example = "PAR-0001")
	public String codigo_autogenerado;

	@Schema(description = "DNI del productor, exactamente 8 dígitos. Puede repetirse en varios registros de parcela.", minLength = 8, maxLength = 8, pattern = "^[0-9]{8}$", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345678")
	public String dni_productor;

	@Schema(description = "Identificador de parcela por productor (DNI + secuencial), generado automáticamente por la base de datos; no es necesario enviarlo.", accessMode = Schema.AccessMode.READ_ONLY, maxLength = 50, example = "12345678_1")
	public String n_parcela;

	@Schema(description = "Nombres del productor", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED, example = "Ana")
	public String nombres;

	@Schema(description = "Apellido paterno del productor", maxLength = 100, example = "Perez")
	public String apellido_paterno;

	@Schema(description = "Apellido materno del productor", maxLength = 100, example = "Garcia")
	public String apellido_materno;

	@Schema(description = "Nombre completo del productor", maxLength = 300, example = "Ana Perez Garcia")
	public String nombre_completo;

	@Schema(description = "Fecha de nacimiento del productor", type = "string", format = "date", example = "1985-06-15")
	public LocalDate fecha_nacimiento;

	@Schema(description = "Sexo del productor: M o F", allowableValues = { "M", "F" }, example = "F")
	public String sexo;

	@Schema(description = "Celular peruano del participante; debe comenzar en 9 y tener 9 dígitos.", maxLength = 9, pattern = "^9[0-9]{8}$", example = "987654321")
	public String celular_participante;

	@Schema(description = "Tipo de productor", maxLength = 100, example = "Agricultor")
	public String tipo_productor;

	@Schema(description = "Actividad agraria", maxLength = 150, example = "Agricultura")
	public String actividad_agraria;

	@Schema(description = "Tipo de cultivo", maxLength = 100, example = "Cafe")
	public String tipo_cultivo;

	@Schema(description = "Variedad del cultivo", maxLength = 100, example = "01")
	public String variedad;

	@Schema(description = "Régimen de tenencia", maxLength = 100, example = "Propietario")
	public String regimen_tenencia;

	@Schema(description = "Superficie registrada por MIDAGRI en hectáreas; si se informa debe ser mayor que cero.", example = "2.5")
	public BigDecimal superficie_midagri;

	@Schema(description = "Oficina zonal", maxLength = 100, example = "La Merced")
	public String oficina_zonal;

	@Schema(description = "Código ubigeo de seis dígitos", minLength = 6, maxLength = 6, pattern = "^[0-9]{6}$", example = "120101")
	public String ubigeo;

	@Schema(description = "Departamento", maxLength = 100, example = "Junin")
	public String departamento;

	@Schema(description = "Provincia", maxLength = 100, example = "Chanchamayo")
	public String provincia;

	@Schema(description = "Distrito", maxLength = 100, example = "La Merced")
	public String distrito;

	@Schema(description = "Caserío o centro poblado", maxLength = 200, example = "San Juan")
	public String caserio;

	@Schema(description = "Geometría WKT de tipo POLYGON o LINESTRING (2D o Z) en SRID 4326. Coordenadas en orden longitud, latitud y, si aplica, altitud. El esquema PostGIS debe permitir ambos tipos.", type = "string", requiredMode = Schema.RequiredMode.REQUIRED, example = "POLYGON Z ((-77.00063113 -12.00041125 1250.5, -76.99936887 -12.00041125 1250.5, -76.99936887 -11.99958875 1250.5, -77.00063113 -11.99958875 1250.5, -77.00063113 -12.00041125 1250.5))", examples = {
				"POLYGON Z ((-77.00063113 -12.00041125 1250.5, -76.99936887 -12.00041125 1250.5, -76.99936887 -11.99958875 1250.5, -77.00063113 -11.99958875 1250.5, -77.00063113 -12.00041125 1250.5))",
				"LINESTRING Z (-77 -12 1250.5, -76.9 -11.9 1251.0)" })
	public String geom;

	@Schema(description = "Centroide WKT de tipo POINT en SRID 4326", type = "string", example = "POINT(-76.95 -11.95)")
	public String centroide;

	@Schema(description = "Área de la parcela en hectáreas", example = "1.25")
	public BigDecimal area_ha;

	@Schema(description = "Perímetro de la parcela en metros", example = "456.78")
	public BigDecimal perimetro_m;

	@Schema(description = "Altitud en metros", example = "1250.5")
	public BigDecimal altitud;

	@Schema(description = "Fuente del registro", maxLength = 30, example = "GPS")
	public String fuente;

	@Schema(description = "Datum geodésico; obligatorio en la tabla de destino.", maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED, example = "WGS84")
	public String datum;

	@Schema(description = "Observaciones del registro", example = "Parcela levantada en campo")
	public String observaciones;

	@Schema(description = "DNI del profesional, exactamente 8 dígitos.", minLength = 8, maxLength = 8, pattern = "^[0-9]{8}$", requiredMode = Schema.RequiredMode.REQUIRED, example = "87654321")
	public String profesional_dni;

	@Schema(description = "Nombres del profesional", maxLength = 100, example = "Luis")
	public String profesional_nombres;

	@Schema(description = "Apellidos del profesional", maxLength = 150, example = "Gomez Ruiz")
	public String profesional_apellidos;

	@Schema(description = "Celular del profesional", maxLength = 9, example = "912345678")
	public String profesional_celular;

	@Schema(description = "Correo electrónico del profesional", maxLength = 255, example = "luis.gomez@devida.gob.pe")
	public String profesional_email;

	@Schema(description = "URL HTTPS fuente opcional de la imagen del anverso del DNI. El host debe estar configurado; el servicio descarga la imagen y guarda aquí la URL API generada con UUID. No enviar Base64.", example = "https://almacenamiento/ruta/dni-anverso.jpg")
	public String ruta_dni_anverso;

	@Schema(description = "URL HTTPS fuente opcional de la imagen del reverso del DNI. El host debe estar configurado; el servicio descarga la imagen y guarda aquí la URL API generada con UUID. No enviar Base64.", example = "https://almacenamiento/ruta/dni-reverso.jpg")
	public String ruta_dni_reverso;

	@Schema(description = "URL HTTPS fuente opcional de la foto 1. El host debe estar configurado; el servicio descarga la imagen y guarda aquí la URL API generada con UUID. No enviar Base64.", example = "https://almacenamiento/ruta/foto1.jpg")
	public String ruta_foto1;

	@Schema(description = "URL HTTPS fuente opcional de la foto 2. El host debe estar configurado; el servicio descarga la imagen y guarda aquí la URL API generada con UUID. No enviar Base64.", example = "https://almacenamiento/ruta/foto2.jpg")
	public String ruta_foto2;

	@Schema(description = "URL HTTPS fuente opcional de la foto 3. El host debe estar configurado; el servicio descarga la imagen y guarda aquí la URL API generada con UUID. No enviar Base64.", example = "https://almacenamiento/ruta/foto3.jpg")
	public String ruta_foto3;

	@Schema(description = "URL HTTPS fuente opcional de la foto 4. El host debe estar configurado; el servicio descarga la imagen y guarda aquí la URL API generada con UUID. No enviar Base64.", example = "https://almacenamiento/ruta/foto4.jpg")
	public String ruta_foto4;

	@Schema(description = "URL HTTPS fuente opcional de la foto 5. El host debe estar configurado; el servicio descarga la imagen y guarda aquí la URL API generada con UUID. No enviar Base64.", example = "https://almacenamiento/ruta/foto5.jpg")
	public String ruta_foto5;

	@Schema(description = "URL HTTPS fuente opcional de la foto 6. El host debe estar configurado; el servicio descarga la imagen y guarda aquí la URL API generada con UUID. No enviar Base64.", example = "https://almacenamiento/ruta/foto6.jpg")
	public String ruta_foto6;

	@Schema(description = "Identificador único del dispositivo móvil", maxLength = 64, example = "device-uuid")
	public String device_uuid;

	@Schema(description = "Fecha y hora de captura registradas por el móvil, con offset ISO 8601", type = "string", format = "date-time", example = "2026-11-03T15:00:00-05:00")
	public OffsetDateTime fecha_captura;

	@Schema(description = "Fecha y hora de creación, con offset ISO 8601; obligatoria en la tabla de destino.", type = "string", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-11-03T15:00:00-05:00")
	public OffsetDateTime fecha_creacion;

	@Schema(description = "Fecha y hora de última actualización, con offset ISO 8601; obligatoria en la tabla de destino.", type = "string", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-11-03T15:00:00-05:00")
	public OffsetDateTime fecha_actualizacion;

	@Schema(description = "Marca de borrado lógico sincronizada desde el móvil; obligatoria en la tabla de destino.", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
	public Boolean eliminado;
}
