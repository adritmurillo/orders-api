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
${DB_URL}, ${DB_USER}, ${DB_PASSWORD}. Asi que para que el sistema arranque de forma adecuada, asegurese de tener definidas las credenciales mencionadas: 

* DB_URL: URL de conexión (ej: jdbc:postgresql://localhost:5432/orders_db)
* DB_USER: Tu usuario de Postgres (ej: postgres)
* DB_PASSWORD: Tu contraseña

De cualquier forma, para simplificar el arranque del sistema, puedes ejecutar los siguientes comandos colocando ahí tus credenciales, sin la necesidad de configurar las variables de entorno

### 4. Ejecutar la aplicacion

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
