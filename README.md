<p align="center">
  <h1 align="center">SeccHub Backend</h1>

  <p align="center">
    Sistema de Gestión Organizacional para la Pontificia Universidad Javeriana
    <br />
    <strong>Automatización de Creación y Asignación de Clases</strong>
  </p>
</p>

---
## Tabla de Contenidos

- [Tabla de Contenidos](#tabla-de-contenidos)
- [Acerca del Proyecto](#acerca-del-proyecto)
  - [Módulos Principales](#módulos-principales)
  - [Construido Con](#construido-con)
    - [Frameworks Principales](#frameworks-principales)
    - [Herramientas de Desarrollo](#herramientas-de-desarrollo)
    - [Herramientas Adicionales](#herramientas-adicionales)
- [Prerequisitos](#prerequisitos)
  - [Para Despliegue con Docker (Recomendado)](#para-despliegue-con-docker-recomendado)
  - [Para Despliegue Manual](#para-despliegue-manual)
  - [Herramientas Opcionales](#herramientas-opcionales)
- [Instalación y Configuración](#instalación-y-configuración)
  - [Configuración de la Aplicación](#configuración-de-la-aplicación)
    - [Archivo de Variables de Entorno (.env)](#archivo-de-variables-de-entorno-env)
      - [Variables de Base de Datos](#variables-de-base-de-datos)
      - [Variables de Correo Electrónico](#variables-de-correo-electrónico)
      - [Variables de Seguridad JWT](#variables-de-seguridad-jwt)
      - [Configuración de Logging](#configuración-de-logging)
  - [Configuración de Docker Compose](#configuración-de-docker-compose)
    - [Variables de Entorno del Servicio de Base de Datos](#variables-de-entorno-del-servicio-de-base-de-datos)
    - [Scripts de Inicialización de Base de Datos](#scripts-de-inicialización-de-base-de-datos)
    - [Eliminar Datos de Prueba (Mock Data) para Producción](#eliminar-datos-de-prueba-mock-data-para-producción)
    - [Variables de Entorno del Servicio de Aplicación](#variables-de-entorno-del-servicio-de-aplicación)
    - [Ejemplo de docker-compose.yaml para Producción](#ejemplo-de-docker-composeyaml-para-producción)
  - [Despliegue con Docker Compose](#despliegue-con-docker-compose)
    - [Paso 1: Clonar el Repositorio](#paso-1-clonar-el-repositorio)
    - [Paso 2: Configurar Variables de Entorno](#paso-2-configurar-variables-de-entorno)
    - [Paso 3: Construir y Desplegar](#paso-3-construir-y-desplegar)
    - [Paso 4: Verificar el Despliegue](#paso-4-verificar-el-despliegue)
    - [Comandos Útiles de Docker Compose](#comandos-útiles-de-docker-compose)
  - [Despliegue Manual](#despliegue-manual)
    - [Paso 1: Configurar MySQL](#paso-1-configurar-mysql)
    - [Paso 2: Configurar Variables de Entorno](#paso-2-configurar-variables-de-entorno-1)
    - [Paso 3: Compilar y Ejecutar](#paso-3-compilar-y-ejecutar)
- [Mantenimiento](#mantenimiento)
  - [Actualización de Dependencias](#actualización-de-dependencias)
    - [Versiones Actuales del Proyecto](#versiones-actuales-del-proyecto)
    - [Verificar Actualizaciones Disponibles](#verificar-actualizaciones-disponibles)
    - [Actualizar Dependencias](#actualizar-dependencias)
    - [Calendario de Actualizaciones Recomendado](#calendario-de-actualizaciones-recomendado)
  - [Actualizaciones de Seguridad](#actualizaciones-de-seguridad)
    - [Verificar Vulnerabilidades](#verificar-vulnerabilidades)
    - [Actualizar Imágenes Docker](#actualizar-imágenes-docker)
- [Pruebas](#pruebas)
  - [Ejecutar Suite de Pruebas Completa](#ejecutar-suite-de-pruebas-completa)
  - [Ejecutar Pruebas Específicas](#ejecutar-pruebas-específicas)
  - [Pruebas de Integración](#pruebas-de-integración)
  - [Verificación de Calidad de Código](#verificación-de-calidad-de-código)
- [Equipo de Desarrollo](#equipo-de-desarrollo)

---
## Acerca del Proyecto

**SeccHub** es un Sistema de Gestión Organizacional que automatiza el proceso de creación y asignación de clases dentro del Departamento de Ingeniería de Sistemas de la Pontificia Universidad Javeriana. El sistema fue diseñado con un carácter modular y extensible, con la finalidad de poder expandir sobre el mismo en trabajos futuros de ser necesario.

### Módulos Principales

El sistema cuenta con los siguientes módulos principales:

- **Módulo de Administración (Admin)**: Gestiona la información base del sistema incluyendo cursos, secciones, semestres, profesores y registros académicos. Proporciona las operaciones CRUD fundamentales para las entidades administrativas del departamento.

- **Módulo de Integración (Integration)**: Apoya la consolidación de solicitudes académicas y automatiza la unificación y verificación de solicitudes de estudiantes provenientes de diferentes carreras. Gestiona solicitudes de estudiantes, clases de profesores y peticiones académicas.

- **Módulo de Planeación (Planning)**: Ayuda con la asignación y planeación de clases, incluyendo la gestión de aulas, horarios, profesores y asistentes de enseñanza. Maneja la detección de conflictos de horarios y la asignación óptima de recursos.

- **Módulo de Notificaciones (Notification)**: Provee notificaciones automáticas por correo electrónico a los profesores y personal administrativo sobre eventos importantes del sistema.

- **Módulo de Seguridad (Security)**: Maneja la autenticación y autorización de usuarios mediante JWT, gestión de roles y control de acceso a los recursos del sistema.

- **Módulo Paramétrico (Parametric)**: Gestiona los parámetros y configuraciones del sistema que controlan el comportamiento de los diferentes módulos.

- **Módulo de Auditoría (Log)**: Registra las operaciones y eventos importantes del sistema para fines de auditoría y trazabilidad.

### Construido Con

#### Frameworks Principales

- [Spring Boot 3.5.7](https://spring.io/projects/spring-boot) - Framework principal de la aplicación
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html) - Framework reactivo para aplicaciones web
- [Spring Security](https://spring.io/projects/spring-security) - Framework de autenticación y autorización
- [Spring Modulith 1.4.1](https://spring.io/projects/spring-modulith) - Arquitectura modular
- [R2DBC](https://r2dbc.io/) - Reactive Relational Database Connectivity
- [MySQL 8.4.6](https://www.mysql.com/) - Base de datos relacional

#### Herramientas de Desarrollo

- [Java 21](https://openjdk.org/projects/jdk/21/) - Lenguaje de programación
- [Maven 3.9.11](https://maven.apache.org/) - Gestión de dependencias y construcción
- [Docker](https://www.docker.com/) - Contenedorización
- [JWT (jjwt 0.13.0)](https://github.com/jwtk/jjwt) - Autenticación basada en tokens
- [Lombok](https://projectlombok.org/) - Reducción de código boilerplate
- [JUnit 5.14.0](https://junit.org/junit5/) - Framework de testing
- [Testcontainers 2.0.1](https://www.testcontainers.org/) - Testing con contenedores
- [JaCoCo 0.8.14](https://www.jacoco.org/) - Cobertura de código

#### Herramientas Adicionales

- [ModelMapper 3.2.0](http://modelmapper.org/) - Mapeo de objetos
- [Caffeine](https://github.com/ben-manes/caffeine) - Cache de alto rendimiento
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) - Monitoreo y métricas

---
## Prerequisitos

Antes de comenzar con el despliegue del aplicativo, asegúrese de tener instalados los siguientes componentes:

### Para Despliegue con Docker (Recomendado)

- **Docker Engine 28.0 o superior**
  ```sh
  docker --version
  ```

- **Docker Compose 2.40.0 o superior**
  ```sh
  docker compose version
  ```

### Para Despliegue Manual

- **Java Development Kit (JDK) 21**
  ```sh
  java -version
  ```

- **Apache Maven 3.9.11 o superior**
  ```sh
  mvn -version
  ```

- **MySQL 8.0 o superior**
  ```sh
  mysql --version
  ```

### Herramientas Opcionales

- **Git** (para clonar el repositorio)
  ```sh
  git --version
  ```

- **curl** (para health checks)
  ```sh
  curl --version
  ```

---
## Instalación y Configuración

### Configuración de la Aplicación

La aplicación se configura mediante el archivo `src/main/resources/application.yaml` y variables de entorno. Todas las configuraciones sensibles **deben** sobrescribirse mediante variables de entorno para producción.

#### Archivo de Variables de Entorno (.env)

Antes de desplegar la aplicación, es ***CRÍTICO*** configurar las variables de entorno para producción. Cree un archivo `.env` en la raíz del proyecto o cree una configuración homóloga a:

```bash
# ==============================================================================
# VARIABLES DE ENTORNO - PRODUCCIÓN
# ==============================================================================

# Base de Datos
DB_URL=r2dbc:mysql://localhost:3306/secchub     # Es necesario que el API use R2DBC
DB_USERNAME=secchub_user                        # Usuario de la base de datos
DB_PASSWORD=CONTRASEÑA_SECRETA                  # Contraseña de la base de datos

# Correo Electrónico (Gmail)
MAIL_HOST=smtp.gmail.com                        # Servidor SMTP de Gmail
MAIL_PORT=587                                   # Puerto SMTP de Gmail
MAIL_USERNAME=secchub@javeriana.edu.co          # Usuario de correo
MAIL_PASSWORD=CAMBIAR_ESTA_CONTRASEÑA           # Contraseña de correo (usar App Password)

# JWT (CRÍTICO: Cambiar en producción)
JWT_SECRET=GENERAR_UN_SECRET_KEY_SEGURO_DE_AL_MENOS_512_BITS    # Clave secreta para firmar tokens JWT
JWT_EXPIRATION=86400000                                         # Tiempo de expiración del token (ms)
JWT_REFRESH_EXPIRATION=604800000                                # Tiempo de expiración del refresh token (ms)       
JWT_ISSUER=secchub.javeriana.edu.co                             # Emisor del token JWT (dirección del sistema)
```

##### Variables de Base de Datos

| Variable | Descripción | Valor por Defecto | Requerido |
|----------|-------------|-------------------|-----------|
| `DB_URL` | URL de conexión R2DBC | `r2dbc:mysql://localhost:3306/secchub` | ✅ |
| `DB_USERNAME` | Usuario de la base de datos | `user` | ✅ |
| `DB_PASSWORD` | Contraseña de la base de datos | `password` | ✅ |

**Ejemplo en application.yaml:**
```yaml
spring:
  r2dbc:
    url: ${DB_URL:r2dbc:mysql://localhost:3306/secchub}
    username: ${DB_USERNAME:user}
    password: ${DB_PASSWORD:password}
```

##### Variables de Correo Electrónico

| Variable | Descripción | Valor por Defecto | Requerido |
|----------|-------------|-------------------|-----------|
| `MAIL_HOST` | Servidor SMTP | `smtp.gmail.com` | ✅ |
| `MAIL_PORT` | Puerto SMTP | `587` | ✅ |
| `MAIL_USERNAME` | Usuario de correo | `username` | ✅ |
| `MAIL_PASSWORD` | Contraseña de correo | `password` | ✅ |

**Configuración para Gmail:**
1. Habilitar autenticación de dos factores en su cuenta de Google
2. Generar una [App Password](https://support.google.com/accounts/answer/185833)
3. Usar la App Password generada como `MAIL_PASSWORD`

##### Variables de Seguridad JWT

| Variable | Descripción | Valor por Defecto | Requerido |
|----------|-------------|-------------------|-----------|
| `JWT_SECRET` | Clave secreta para firmar tokens | Valor de desarrollo | ✅ |
| `JWT_EXPIRATION` | Tiempo de expiración del token (ms) | `86400000` (24h) | ❌ |
| `JWT_REFRESH_EXPIRATION` | Tiempo de expiración del refresh token (ms) | `604800000` (7d) | ❌ |
| `JWT_ISSUER` | Emisor del token JWT | `secchub.javeriana.edu.co` | ❌ |

**CRÍTICO - Generación de JWT_SECRET seguro:**

```bash
# Generar un secret key seguro de 512 bits
openssl rand -base64 64

# O usando Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

**Tiempos de expiración recomendados:**
- **Desarrollo**: 24 horas (86400000 ms)
- **Producción (alta seguridad)**: 1 hora (3600000 ms)
- **Producción (balance)**: 12 horas (43200000 ms)

##### Configuración de Logging

La aplicación genera logs en dos formatos:

1. **Consola** (con colores): Para debugging y desarrollo
2. **Archivo**: `logs/secchub-backend.log`

**Niveles de logging disponibles:**

| Nivel | Descripción | Uso |
|-------|-------------|-----|
| `TRACE` | Máximo detalle (incluye parámetros SQL) | Solo debugging |
| `DEBUG` | Información detallada | Desarrollo |
| `INFO` | Información general | **Recomendado para producción** |
| `WARN` | Advertencias | Producción |
| `ERROR` | Errores | Producción |

**Modificar niveles de logging mediante variables del yaml:**

```yaml
logging:
    level:
        "[co.edu.puj.secchub_backend]": INFO
        "[org.springframework.security]": INFO
        "[org.springframework.web]": INFO
```

**Advertencia de Seguridad:**
- `DEBUG` y `TRACE` pueden exponer información sensible en logs
- En producción, usar nivel `INFO` o superior
- Rotar logs regularmente para evitar uso excesivo de disco

---
### Configuración de Docker Compose

El archivo `docker-compose.yaml` define los servicios necesarios para ejecutar la aplicación. A continuación se describen las configuraciones importantes:

#### Variables de Entorno del Servicio de Base de Datos

El servicio `db` utiliza las siguientes variables de entorno para configurar MySQL:

```yaml
environment:
  MYSQL_ROOT_PASSWORD: rootpassword    # Contraseña del usuario root de MySQL
  MYSQL_DATABASE: secchub              # Nombre de la base de datos a crear
  MYSQL_USER: user                     # Usuario de la base de datos
  MYSQL_PASSWORD: password             # Contraseña del usuario
```

**IMPORTANTE para Producción**: Cambie estas credenciales por valores seguros. Puede usar variables de entorno:

```yaml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-rootpassword}
  MYSQL_DATABASE: ${MYSQL_DATABASE:-secchub}
  MYSQL_USER: ${MYSQL_USER:-user}
  MYSQL_PASSWORD: ${MYSQL_PASSWORD:-password}
```

Y agregar al archivo `.env`:
```bash
# Credenciales de MySQL
MYSQL_ROOT_PASSWORD=su_contraseña_root_segura
MYSQL_DATABASE=secchub
MYSQL_USER=secchub_user
MYSQL_PASSWORD=su_contraseña_segura
```

#### Scripts de Inicialización de Base de Datos

El contenedor de MySQL ejecuta automáticamente los scripts SQL montados en `/docker-entrypoint-initdb.d/` en orden alfabético:

```yaml
volumes:
  - ./docker/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql              # REQUERIDO: Esquema de base de datos
  - ./docker/init-parameters.sql:/docker-entrypoint-initdb.d/02-init.sql       # REQUERIDO: Parámetros del sistema
  - ./docker/init-mock-data.sql:/docker-entrypoint-initdb.d/03-init.sql        # OPCIONAL: Datos de prueba
```

**Descripción de los scripts:**

1. **`schema.sql`** (REQUERIDO): Crea todas las tablas, índices, claves foráneas y estructura de la base de datos.

2. **`init-parameters.sql`** (REQUERIDO): Inserta parámetros de configuración del sistema necesarios para el funcionamiento correcto de la aplicación.

3. **`init-mock-data.sql`** (OPCIONAL): Contiene datos de prueba para desarrollo y testing. **NO debe usarse en producción.**

#### Eliminar Datos de Prueba (Mock Data) para Producción

Para despliegues en producción, **elimine o comente** la línea del script de datos de prueba en `docker-compose.yaml`:

```yaml
volumes:
  - mysql_data:/var/lib/mysql
  - ./docker/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
  - ./docker/init-parameters.sql:/docker-entrypoint-initdb.d/02-init.sql
  # - ./docker/init-mock-data.sql:/docker-entrypoint-initdb.d/03-init.sql    # Comentar o eliminar esta línea
```

#### Variables de Entorno del Servicio de Aplicación

El servicio `app` debe configurarse con las siguientes variables de entorno. Edite el `docker-compose.yaml` para usar variables de entorno desde el archivo `.env`:

```yaml
app:
  environment:
    # Base de datos (debe usar R2DBC)
    DB_URL: ${DB_URL:-r2dbc:mysql://localhost:3306/secchub}
    DB_USERNAME: ${DB_USERNAME:-user}
    DB_PASSWORD: ${DB_PASSWORD:-password}
    
    # Correo electrónico
    MAIL_HOST: ${MAIL_HOST:-smtp.gmail.com}
    MAIL_PORT: ${MAIL_PORT:-587}
    MAIL_USERNAME: ${MAIL_USERNAME}
    MAIL_PASSWORD: ${MAIL_PASSWORD}
    
    # JWT
    JWT_SECRET: ${JWT_SECRET}
    JWT_EXPIRATION: ${JWT_EXPIRATION:-86400000}
    JWT_REFRESH_EXPIRATION: ${JWT_REFRESH_EXPIRATION:-604800000}
    JWT_ISSUER: ${JWT_ISSUER:-secchub.javeriana.edu.co}
```

**Nota**: El archivo `docker-compose.yaml` actual usa configuraciones simplificadas. Para producción, se recomienda modificarlo para usar las variables de entorno del archivo `.env` como se muestra arriba.

#### Ejemplo de docker-compose.yaml para Producción

```yaml
services:
  db:
    image: mysql:8.4.6
    container_name: secchub-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-rootpassword}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-secchub}
      MYSQL_USER: ${MYSQL_USER:-user}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-password}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./docker/init-parameters.sql:/docker-entrypoint-initdb.d/02-init.sql
      # Mock data comentado para producción
      # - ./docker/init-mock-data.sql:/docker-entrypoint-initdb.d/03-init.sql
    network_mode: "host"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 5s
      retries: 10

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: secchub-backend
    restart: unless-stopped
    environment:
      DB_URL: ${DB_URL:-r2dbc:mysql://localhost:3306/secchub}
      DB_USERNAME: ${DB_USERNAME:-user}
      DB_PASSWORD: ${DB_PASSWORD:-password}
      MAIL_HOST: ${MAIL_HOST:-smtp.gmail.com}
      MAIL_PORT: ${MAIL_PORT:-587}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION:-86400000}
      JWT_REFRESH_EXPIRATION: ${JWT_REFRESH_EXPIRATION:-604800000}
      JWT_ISSUER: ${JWT_ISSUER:-secchub.javeriana.edu.co}
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    network_mode: "host"

volumes:
  mysql_data:
    driver: local
    name: mysql_data
```

---
### Despliegue con Docker Compose

Este es el método **recomendado** para desplegar la aplicación en cualquier entorno.

#### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/ElCzar/secchub-backend.git
cd secchub-backend
```

#### Paso 2: Configurar Variables de Entorno

Asegúrese de que el archivo `.env` esté configurado correctamente (ver sección anterior).

#### Paso 3: Construir y Desplegar

```bash
# Construir y levantar los contenedores en modo detached
docker compose up --build -d
```

Este comando realizará las siguientes acciones:
1. Construye la imagen Docker de la aplicación usando el Dockerfile multi-stage
2. Descarga la imagen de MySQL 8.4.6
3. Crea un volumen persistente para los datos de MySQL
4. Inicializa la base de datos con los scripts SQL en `/docker`
5. Levanta ambos servicios (db y app) en modo background

#### Paso 4: Verificar el Despliegue

```bash
# Ver los logs de la aplicación
docker compose logs -f app

# Ver el estado de los contenedores
docker compose ps

# Verificar el health check
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

#### Comandos Útiles de Docker Compose

```bash
# Detener los servicios
docker compose stop

# Iniciar los servicios detenidos
docker compose start

# Reiniciar los servicios
docker compose restart

# Ver logs en tiempo real
docker compose logs -f

# Ver logs solo de la aplicación
docker compose logs -f app

# Ver logs solo de la base de datos
docker compose logs -f db

# Detener y eliminar contenedores (mantiene volúmenes)
docker compose down

# Detener y eliminar contenedores y volúmenes (ELIMINA DATOS)
docker compose down -v

# Reconstruir la imagen sin cache
docker compose build --no-cache

# Ver uso de recursos
docker stats
```

---
### Despliegue Manual

Si prefiere no usar Docker, puede desplegar la aplicación manualmente.

#### Paso 1: Configurar MySQL

```bash
# Conectarse a MySQL
mysql -u root -p

# Crear base de datos y usuario
CREATE DATABASE secchub;
CREATE USER 'secchub_user'@'localhost' IDENTIFIED BY 'CONTRASEÑA_SEGURA';
GRANT ALL PRIVILEGES ON secchub.* TO 'secchub_user'@'localhost';
FLUSH PRIVILEGES;

# Ejecutar scripts de inicialización
mysql -u secchub_user -p secchub < docker/schema.sql            # Requirido
mysql -u secchub_user -p secchub < docker/init-parameters.sql   # Requirido
mysql -u secchub_user -p secchub < docker/init-mock-data.sql    # Opcional
```

#### Paso 2: Configurar Variables de Entorno

```bash
# Linux/macOS
export DB_URL="r2dbc:mysql://localhost:3306/secchub"
export DB_USERNAME="secchub_user"
export DB_PASSWORD="CONTRASEÑA_SEGURA"
export MAIL_USERNAME="secchub@javeriana.edu.co"
export MAIL_PASSWORD="CONTRASEÑA_CORREO"
export JWT_SECRET="SECRET_KEY_SEGURO"

# Windows (PowerShell)
$env:DB_URL="r2dbc:mysql://localhost:3306/secchub"
$env:DB_USERNAME="secchub_user"
$env:DB_PASSWORD="CONTRASEÑA_SEGURA"
# ... etc
```

#### Paso 3: Compilar y Ejecutar

```bash
# Compilar el proyecto
mvn clean package -DskipTests

# Ejecutar la aplicación
java -jar target/secchub-backend-1.0.jar
```

O usando Maven directamente:

```bash
mvn spring-boot:run
```

---
## Mantenimiento

### Actualización de Dependencias

#### Versiones Actuales del Proyecto

Las versiones de las dependencias se gestionan en el archivo `pom.xml`:

```xml
<properties>
    <java.version>21</java.version>
    <spring-modulith.version>1.4.1</spring-modulith.version>
    <jjwt.version>0.13.0</jjwt.version>
    <modelmapper.version>3.2.0</modelmapper.version>
    <testcontainers.version>2.0.1</testcontainers.version>
    <junit.jupiter.version>5.14.0</junit.jupiter.version>
    <jacoco.version>0.8.14</jacoco.version>
</properties>
```

**Spring Boot Parent:** `3.5.7`

#### Verificar Actualizaciones Disponibles

```bash
# Ver todas las dependencias desactualizadas
mvn versions:display-dependency-updates

# Ver actualizaciones de plugins
mvn versions:display-plugin-updates

# Ver actualizaciones de propiedades
mvn versions:display-property-updates
```

#### Actualizar Dependencias

**Importante**: Siempre realizar pruebas completas después de actualizar dependencias para asegurar la compatibilidad.

```bash
# 1. Actualizar Spring Boot Parent Version
mvn versions:update-parent

# 2. Actualizar dependencias específicas
mvn versions:use-latest-releases

# 3. Compilar y probar
mvn clean install

# 4. Ejecutar suite de pruebas completa
mvn test

# 5. Verificar cobertura de código
mvn jacoco:report
```

#### Calendario de Actualizaciones Recomendado

| Componente | Frecuencia |
|------------|-----------|
| Parches de seguridad | Inmediatamente |
| Spring Boot (minor) | Mensual |
| Spring Boot (major) | Por release |
| Dependencias (patch) | Mensual |
| Dependencias (minor) | Trimestral |
| Java (LTS) | Anual |

---
### Actualizaciones de Seguridad

#### Verificar Vulnerabilidades

```bash
# Análisis de seguridad con Maven
mvn dependency-check:check

# Ver reporte de vulnerabilidades
open target/dependency-check-report.html
```

#### Actualizar Imágenes Docker

```bash
# Actualizar imagen base de la aplicación
# Editar Dockerfile y cambiar versión de eclipse-temurin

# Actualizar imagen de MySQL en docker-compose.yaml
# Cambiar: mysql:8.4.6 a la versión más reciente

# Reconstruir con nuevas imágenes
docker compose build --no-cache
docker compose up -d
```

---
## Pruebas

### Ejecutar Suite de Pruebas Completa

```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar pruebas con reporte de cobertura
mvn clean test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

---
### Ejecutar Pruebas Específicas

```bash
# Ejecutar una clase de prueba específica
mvn test -Dtest=CourseServiceTest

# Ejecutar un método de prueba específico
mvn test -Dtest=CourseServiceTest#testCreateCourse

# Ejecutar todas las pruebas de un paquete
mvn test -Dtest=co.edu.puj.secchub_backend.admin.**
```

---
### Pruebas de Integración

```bash
# Ejecutar solo pruebas de integración
mvn test -Dtest=**/*IntegrationTest

# Ejecutar pruebas con Testcontainers
mvn verify
```

---
### Verificación de Calidad de Código

```bash
# Generar reporte de cobertura
mvn jacoco:report

# Ver métricas de cobertura en consola
mvn jacoco:check
```

**Ubicación de reportes:**
- Reporte HTML de JaCoCo: `target/site/jacoco/index.html`
- Reportes de Surefire: `target/surefire-reports/`

---
## Equipo de Desarrollo

**Desarrolladores:**

- **Ana Sofía Rodríguez Martínez** - [anarodriguezm@javeriana.edu.co](mailto:anarodriguezm@javeriana.edu.co)

- **César Andrés Olarte Marín** - [olartecesar@javeriana.edu.co](mailto:olartecesar@javeriana.edu.co)

- **David Ricardo Gutierrez González** - [dgutierrez@javeriana.edu.co](mailto:dgutierrez@javeriana.edu.co)

- **Paola Benítez Ruiz** - [benitezpaola@javeriana.edu.co](mailto:benitezpaola@javeriana.edu.co)

**Repositorio del Proyecto:** [https://github.com/ElCzar/secchub-backend](https://github.com/ElCzar/secchub-backend)

**Repositorio componente frontend:** [https://github.com/ElCzar/secchub-frontend](https://github.com/ElCzar/secchub-frontend)

---

<p align="center">
  <strong>SeccHub Backend v1.0</strong><br>
  Desarrollado con ❤️ por estudiantes de la Pontificia Universidad Javeriana<br>
</p>
