# User Service - Microservicio de Usuarios

## 📋 Descripción
Microservicio desarrollado en Spring Boot para la creación y consulta de usuarios con autenticación JWT, desarrollado como parte de la evaluación técnica de BCI.

## 🛠 Tecnologías Utilizadas
- **Java 11** (OpenJDK)
- **Spring Boot 2.5.14**
- **Spring Data JPA**
- **Spring Security**
- **Gradle 7.4**
- **H2 Database** (en memoria)
- **JWT (JSON Web Tokens)**
- **BCrypt** (encriptación de contraseñas)
- **Spock Framework** (testing)
- **JUnit 5** (testing adicional)
- **JaCoCo** (cobertura de código)

## 🚀 Características de Java 11 Implementadas
1. **`var`** - Inferencia de tipos locales en variables
2. **`Optional.isEmpty()`** - Método agregado en Java 11 para validaciones
3. **`String.isBlank()`** - Método mejorado para validar cadenas vacías
4. **`Collection.toArray()`** - Sobrecarga optimizada para conversiones

## 📋 Requisitos Previos
- **Java 11** o superior
- **Gradle 7.4** o inferior
- **Git** (para clonar el repositorio)
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)

## 🔧 Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/user-service.git
cd user-service
```

### 2. Verificar versión de Java
```bash
java -version
# Debe mostrar Java 11 o superior
```

### 3. Verificar versión de Gradle
```bash
./gradlew --version
# Debe mostrar Gradle 7.4 o inferior
```

## 🏗 Construcción del Proyecto

### 1. Compilar el proyecto
```bash
./gradlew clean build
```

### 2. Ejecutar solo tests
```bash
./gradlew test
```

### 3. Generar reporte de cobertura
```bash
./gradlew jacocoTestReport
```
📊 Reporte disponible en: `build/jacocoHtml/index.html`

### 4. Verificar cobertura mínima (80%)
```bash
./gradlew jacocoTestCoverageVerification
```

## ▶️ Ejecución del Proyecto

### 1. Ejecutar la aplicación
```bash
./gradlew bootRun
```

### 2. Verificar que está funcionando
La aplicación estará disponible en: **http://localhost:8080**

### 3. Acceder a H2 Console (opcional)
- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** `password`

## 📡 API Endpoints

### 🔐 POST /sign-up - Crear Usuario
Crea un nuevo usuario en el sistema.

**Request:**
```bash
curl -X POST http://localhost:8080/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan@domain.com",
    "password": "a2bsfGfd3df",
    "phones": [
      {
        "number": 87650009,
        "citycode": 7,
        "contrycode": "25"
      }
    ]
  }'
```

**Response (201 Created):**
```json
{
  "id": "e5c6cf84-8860-4c00-91cd-22d3be28904e",
  "created": "2025-07-01T15:30:45",
  "lastLogin": "2025-07-01T15:30:45",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "isActive": true,
  "name": "Juan Pérez",
  "email": "juan@domain.com",
  "password": "$2a$10$...",
  "phones": [
    {
      "number": 87650009,
      "citycode": 7,
      "contrycode": "25"
    }
  ]
}
```

### 🔍 GET /login - Consultar Usuario
Consulta la información del usuario autenticado.

**Request:**
```bash
curl -X GET http://localhost:8080/login \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Response (200 OK):**
```json
{
  "id": "e5c6cf84-8860-4c00-91cd-22d3be28904e",
  "created": "2025-07-01T15:30:45",
  "lastLogin": "2025-07-01T15:35:12",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "isActive": true,
  "name": "Juan Pérez",
  "email": "juan@domain.com",
  "password": "$2a$10$...",
  "phones": [...]
}
```

## ✅ Validaciones Implementadas

### 📧 Email
- **Formato:** `aaaaaaa@undominio.algo`
- **Regex:** `^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z]{2,}$`
- **Ejemplo válido:** `juan@domain.com`

### 🔒 Password
- **Requisitos:**
    - Una mayúscula
    - Exactamente dos números (no necesariamente consecutivos)
    - Solo letras minúsculas adicionales
    - Longitud entre 8 y 12 caracteres
- **Regex:** `^(?=.*[A-Z])(?=.*\\d.*\\d)[a-zA-Z\\d]{8,12}$`
- **Ejemplos válidos:**
    - `a2bsfGfd3df`
    - `h1elloW2rld`
    - `test1Pass2`

## ❌ Manejo de Errores

Todos los errores retornan el siguiente formato JSON:

```json
{
  "error": [{
    "timestamp": "2025-07-01T15:30:45",
    "codigo": 400,
    "detail": "Mensaje de error descriptivo"
  }]
}
```

### Códigos de Error HTTP
- **400** - Bad Request (datos inválidos)
- **401** - Unauthorized (token inválido)
- **404** - Not Found (usuario no encontrado)
- **409** - Conflict (usuario ya existe)

## 🧪 Testing

### Ejecutar todos los tests
```bash
./gradlew test
```

### Ver resultados detallados
```bash
./gradlew test --info
```

### Cobertura de código
- **Objetivo:** Mínimo 80%
- **Frameworks:** Spock + JUnit 5
- **Comando:** `./gradlew jacocoTestReport`

## 🏗 Arquitectura del Proyecto

### Estructura de Capas
```
├── Controller     # Endpoints REST
├── Service        # Lógica de negocio
├── Repository     # Acceso a datos
├── Entity         # Modelos JPA
├── DTO            # Transferencia de datos
├── Exception      # Manejo de errores
├── Config         # Configuraciones
└── Util           # Utilidades
```

### Componentes Principales
- **UserController:** Maneja endpoints `/sign-up` y `/login`
- **UserService:** Lógica de negocio de usuarios
- **JwtService:** Generación y validación de tokens
- **UserRepository:** Acceso a datos con Spring Data JPA
- **SecurityConfig:** Configuración de Spring Security

## 🗄 Base de Datos

### Configuración H2
- **Tipo:** En memoria
- **URL:** `jdbc:h2:mem:testdb`
- **Tablas:** `users`, `phones`
- **Relación:** One-to-Many (User → Phone)

### Modelo de Datos
```sql
-- Tabla users
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created TIMESTAMP,
    last_login TIMESTAMP,
    token TEXT,
    is_active BOOLEAN
);

-- Tabla phones
CREATE TABLE phones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    number BIGINT,
    city_code INTEGER,
    country_code VARCHAR(255),
    user_id VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 🔐 Seguridad

### Características Implementadas
- **Encriptación BCrypt** para contraseñas
- **JWT Tokens** para autenticación
- **Validación de tokens** en cada request
- **Spring Security** para configuración
- **Stateless sessions** (sin estado)

### Flujo de Autenticación
1. Usuario se registra con `/sign-up`
2. Sistema genera JWT token
3. Usuario usa token en header `Authorization: Bearer <token>`
4. Sistema valida token en cada request a `/login`

## 📊 Diagramas UML

Los diagramas UML se encuentran en la carpeta `diagrams/`:

- **component-diagram.puml** - Diagrama de componentes del sistema
- **sequence-signup.puml** - Diagrama de secuencia para sign-up
- **sequence-login.puml** - Diagrama de secuencia para login

### Para visualizar los diagramas:
1. Usar herramientas online como PlantUML Server
2. Instalar plugin PlantUML en tu IDE
3. Usar herramientas como Visual Studio Code con extensión PlantUML

## 🚀 Despliegue

### Variables de Entorno
```bash
# Opcional - configurar en application.yml
JWT_SECRET=myVerySecretKeyForJWTTokenGeneration
JWT_EXPIRATION=86400000
SERVER_PORT=8080
```

### Docker (Opcional)
```dockerfile
FROM openjdk:11-jre-slim
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📝 Notas Adicionales

### Consideraciones de Seguridad
- Las contraseñas se almacenan encriptadas con BCrypt
- Los tokens JWT tienen expiración configurable
- La base de datos H2 es solo para desarrollo

### Mejoras Futuras
- Implementar refresh tokens
- Agregar rate limiting
- Usar base de datos persistente
- Implementar logging estructurado
- Agregar métricas y monitoreo

## 👥 Autor
- **Desarrollador:** [Tu Nombre]
- **Email:** [tu.email@domain.com]
- **Fecha:** Julio 2025

## 📄 Licencia
Este proyecto fue desarrollado como parte de una evaluación técnica para BCI.

---

**¡Proyecto listo para evaluación!** ✅

Para cualquier consulta o problema durante la ejecución, revisar los logs de la aplicación o contactar al desarrollador.