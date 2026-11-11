# GeoDAISMovil

Backend Spring Boot para sincronizar registros de parcela con PostgreSQL/PostGIS.

## Configuración

Configura la conexión directamente en `src/main/resources/application.properties`:

- `spring.datasource.url`: URL JDBC de PostgreSQL.
- `spring.datasource.username`: usuario de la base de datos.
- `spring.datasource.password`: contraseña de la base de datos.
- `server.port`: puerto HTTP (por defecto `8080`).

La configuración del almacenamiento de fotos usa estas variables de entorno:

- `GEODAIS_PHOTO_DIRECTORY`: directorio local de imágenes (por defecto `/opt/JBOSSDATA/GEODAIS`). El usuario que ejecuta la aplicación debe tener permisos de lectura y escritura.
- `GEODAIS_PHOTO_PUBLIC_BASE_URL`: URL pública base del servicio, por ejemplo `https://api.midominio.gob.pe`. Si se omite, el registro guardará una ruta relativa del API.
- `GEODAIS_PHOTO_ALLOWED_HOSTS`: lista separada por comas de hosts HTTPS desde los que se permite descargar imágenes. No se descargan imágenes si el host no está en esta lista ni si resuelve a direcciones locales/privadas.
- `GEODAIS_PHOTO_MAX_BYTES`: máximo por imagen en bytes (por defecto 10 MiB).
- `GEODAIS_PHOTO_CONNECT_TIMEOUT_SECONDS` y `GEODAIS_PHOTO_REQUEST_TIMEOUT_SECONDS`: tiempos máximos de conexión y descarga.

La aplicación no modifica automáticamente el esquema. La tabla `public.registros_parcela` y PostGIS deben existir previamente. Antes de probar geometrías, ejecuta `database/allow-linestring-geometry.sql` en la misma base de datos a la que se conecta el backend (`spring.datasource.url`). La migración permite `POINT`, `LINESTRING` y `POLYGON` en `geom`, conserva SRID 4326 y admite coordenadas 2D o 3D. Luego despliega o reinicia el backend con esta versión. Para permitir valores repetidos en `codigo_autogenerado`, ejecuta también `database/allow-duplicate-codigo-autogenerado.sql` en la base existente. Ejecuta `database/add-n-parcela.sql` para agregar `n_parcela` y generar automáticamente un identificador DNI + secuencial en cada registro.

El campo `geom` acepta WKT `POINT`, `LINESTRING` o `POLYGON`; `centroide` continúa siendo un `POINT`. Si la aplicación todavía informa que `geom` solo acepta `Polygon`, verifica que el móvil apunte al backend actualizado, que la migración se haya aplicado a la base de datos usada por ese backend y que el cliente no tenga una validación local que limite el tipo: esa restricción no corresponde a esta versión del backend. El valor de `variedad` (por ejemplo, `"01"`) es texto independiente y no causa errores de geometría.

## Swagger / OpenAPI

Con el servicio iniciado, abre Swagger UI en [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). El documento OpenAPI JSON está disponible en [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

## Endpoints

- `POST /geodaismovil/registros/single`: crea o actualiza el registro identificado por `internal_key`. Recibe en snake_case los campos definidos en `RegistroParcelaRequest` y muestra un ejemplo completo en Swagger, incluidos `codigo_autogenerado`, geometrías y métricas calculadas, timestamps y `eliminado`. `geom` acepta WKT `POINT`, `LINESTRING` o `POLYGON` (2D o Z) en SRID 4326; `centroide` es WKT de punto. Las fechas deben usar formato ISO (`yyyy-MM-dd` para `fecha_nacimiento` y fecha-hora con offset para timestamps). `eliminado` debe ser booleano JSON. Los valores HTTPS en `ruta_dni_anverso`, `ruta_dni_reverso` y `ruta_foto1` a `ruta_foto6` se descargan desde un host configurado en `GEODAIS_PHOTO_ALLOWED_HOSTS`, se guardan en el filesystem con UUID único y se reemplazan por la URL de lectura `/geodaismovil/api/fotos/{uuid}`. Solo se admiten imágenes JPEG o PNG de hasta 10 MiB; no se acepta Base64. Si se omite un campo de foto en una actualización, se conserva su valor anterior.
- `dni_productor` se valida como un DNI peruano de 8 dígitos, pero no es único: se permiten varios registros de parcela para la misma persona. `n_parcela` se genera en la base de datos como DNI + secuencial (por ejemplo, `12345678_1`); el móvil no necesita enviarlo. `internal_key` siempre es único y determina los upserts; `codigo_autogenerado` es opcional y puede repetirse.
- `GET /geodaismovil/api/registros?page=1&limit=10&dni_productor=123`: devuelve resultados paginados, `total`, `totalPages` y `geojson` en cada registro. `dni_productor` permite filtro parcial, insensible a mayúsculas. Se incluyen registros con `eliminado = true`.
- `GET /geodaismovil/api/fotos/{uuid}`: entrega el archivo JPEG/PNG asociado al UUID. El UUID se persiste en la URL de `ruta_*` de la parcela y por ello queda relacionado con su geometría. La URL debe tratarse como una referencia de acceso no predecible; configure autenticación/red privada si se requiere control de acceso por usuario.

Ejemplo mínimo para POST:

```json
{
  "internal_key": "id-local-del-registro",
  "dni_productor": "12345678",
  "nombres": "Ana",
  "profesional_dni": "87654321",
  "geom": "POLYGON Z ((-77  -12 0, -76.9 -12 0, -76.9 -11.9 0, -77 -12 0))",
  "ruta_foto1": "https://almacenamiento/ruta/foto.jpg"
}
```

Los campos no enviados pueden continuar usando los valores predeterminados de la tabla. El backend no depende de un trigger para completar datos: `nombre_completo`, `centroide`, `area_ha` y `perimetro_m` deben incluirse en la solicitud si el cliente móvil es responsable de calcularlos.
