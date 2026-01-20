# Orders API - Prueba Técnica

Se desarrolló el siguiente API REST con el objetivo de cumplir la carga masiva de datos mientras que al mismo tiempo se validan y procesan los pedidos que son enviados en archivos .csv. Este sistema fue hecho con arquitectura hexagonal, por lo que tiene un diseño robusto, escalable pero sobretodo eficiente para manejar grandes volumenes de datos.

---

## Stack Tecnológico Utilizado

* **Lenguaje:** Java 17 
* **Framework:** Spring Boot 3.5.9
* **Base de Datos:** PostgreSQL
* **Migraciones:** Flyway
* **Documentación:** OpenAPI (Swagger) & Postman
* **Testing:** JUnit 5, Mockito, JaCoCo (>80% cobertura)
* **Arquitectura:** Hexagonal (Puertos y Adaptadores)

---

## Instrucciones de Ejecución Local

### 1. Prerrequisitos
* JDK 17 o superior.
* PostgreSQL instalado y corriendo.
* Maven (o usar el wrapper incluido `./mvnw`).

### 2. Configuración de Base de Datos
Crea una base de datos vacía en PostgreSQL:

```sql
CREATE DATABASE orders_db;
```

Cabe resaltar que no será necesario crear las tablas, porque al iniciar la aplicación Flyway las creará automaticamente y de paso agregará datos a las tablas de clientes y zonas. Se puede ver los archivos .sql utilizados en orders-api\src\main\resources\db\migration y apareceran los dos archivos encargados del proceso de creacion y adición de datos a la tablas "clientes" y "zonas".

### 3. Variables de entorno

En la aplicacion, dentro de application.properties, ubicado en orders-api\src\main\resources esta el URL, user y password definidas con variables de entorno:
${DB_URL}, ${DB_USER}, ${DB_PASSWORD}. Asi que para que el sistema arranque de forma adecuada, asegurese de tener definidas las credenciales a mano: 

* DB_URL: URL de conexión (ej: jdbc:postgresql://localhost:5432/orders_db)
* DB_USER: Tu usuario de Postgres (ej: postgres)
* DB_PASSWORD: Tu contraseña

### 4. Ejecutar la aplicacion

De cualquier forma, para simplificar el arranque del sistema, puedes ejecutar estos comandos, colocando ahí tus credenciales y reemplazando donde dice "tu_usuario" por tu usuario real (que suele ser postgres) y "tu_contraseña" por tu contraseña real.

```bash
# Linux / Mac
./mvnw spring-boot:run -Dspring-boot.run.arguments="--DB_USER=tu_usuario --DB_PASSWORD=tu_clave"

# Windows (CMD)
mvnw spring-boot:run -Dspring-boot.run.arguments="--DB_USER=tu_usuario --DB_PASSWORD=tu_clave"
```

La API estará disponible en http://localhost:8080

## Testing y cobertura

Para ejecutar las pruebas unitarias y de integración, incluyendo el reporte de cobertura de JaCoCo:

```bash
./mvnw clean test jacoco:report
```

El reporte HTML estará disponible en: target/site/jacoco/index.html.

## Estrategia de batch (Procesamiento por lotes)

Bueno para hacer que el programa no tenga errores de memoria al momento de cargar grandes archivos se usó la siguiente estrategía: 
* Lectura por Streaming: El archivo CSV no se carga completo en memoria. Se utiliza un flujo de lectura línea por línea.
* Acumulador (Buffer): Los pedidos válidos se acumulan en una lista temporal.
* Persistencia por Lotes: Se define un tamaño de lote configurable (app.batch.size=500). Cuando el acumulador alcanza este límite, se ejecuta una inserción masiva (saveAll) en la base de datos y se libera la memoria.
* Transaccionalidad: Se asegura la integridad de los datos reduciendo el tráfico de red hacia la base de datos.

La clase encargada de este flujo es CargarPedidosService.java la cual implementa al caso de uso con el mismo nombre.
Estas se pueden encontrar en orders-api\src\main\java\com\dinet\orders_api\application\service y orders-api\src\main\java\com\dinet\orders_api\application\ports\input respectivamente.

## Decisiones de diseño

### Arquitectura hexagonal

Se separó el código en distintas capas para separar la lógica del negocio de la infraestructura. 

* Domain: Clases y reglas de negocio puras.

* Application: Casos de uso (CargarPedidosUseCase) que orquestan el flujo y servicios como HashService encargados del algoritmo SHA-256.

* Infrastructure: Controladores REST, Repositorios JPA, adaptadores y más.

### Idempotencia y Seguridad
* Se implementó el header Idempotency-Key obligatorio.
* Se calcula un Hash SHA-256 del contenido del archivo. Si se intenta subir el mismo archivo con la misma llave, el sistema detecta que ya fue procesado y evita la duplicidad.

## Recursos para pruebas

En este mismo repositorio estarán las carpetas /postman y /samples las cuales facilitaran las pruebas. Se recomienda descargar estos archivos: 
* Orders API - Prueba Técnica.postman_collection
* pedidos_validos.csv
* pedidos_con_errores.csv

Para utilizarlas, en primer lugar debemos ejecutar Postman, después darle click a "Import", una vez dentro debemos de cargar el archivo .json que acabas de descargar y listo, automaticamente tendras la coleccion en tu Postman para hacer las pruebas, no necesitas tocar nada mas ya que ahí estará todo configurado, incluso los archivos .csv estarán cargados y listos para darle click en "Send".
