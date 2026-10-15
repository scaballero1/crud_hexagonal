# Gestión de Inmuebles (CRUD con arquitectura hexagonal)

Aplicación de escritorio para gestionar inmuebles (crear, consultar, actualizar y eliminar) sobre una base de datos MySQL, con interfaz gráfica en JavaFX y código organizado en **arquitectura hexagonal** (puertos y adaptadores).

## Qué hace

- Registra **apartamentos** y **casas**. Una casa puede tener un **sótano** opcional.
- Cada inmueble tiene CHIP (identificador único), fecha de compra, dirección, estado y un propietario.
- Los **propietarios** vienen predeterminados en la base de datos; la aplicación no ofrece CRUD sobre ellos.
- Valida los datos en el dominio (CHIP, dirección y propietario obligatorios, fecha no futura, pisos y áreas mayores a 0).

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje |
| JavaFX (controls y fxml) | 21.0.2 | Interfaz gráfica |
| MySQL | 8.x | Base de datos relacional |
| mysql-connector-j | 8.3.0 | Driver JDBC (sin ORM) |
| Maven | 3.9+ | Compilación y ejecución |
| JUnit | 5.10.2 | Pruebas |

## Requisitos previos

- JDK 21 o superior
- Maven 3.9 o superior
- MySQL 8 en ejecución (por defecto en `localhost:3306`)

## Instalación

```bash
git clone <URL-DEL-REPOSITORIO>
cd crud_hexagonal
mvn clean compile
```

## Configuración de la base de datos

1. Asegúrate de que MySQL esté corriendo y de tener un usuario con permisos para crear bases de datos.
2. Edita `resources/application.properties` con tus datos:

   ```properties
   db.url=jdbc:mysql://localhost:3306/inmuebles
   db.user=root
   db.password=tu_contraseña
   ```

3. No necesitas ejecutar scripts a mano. Al arrancar, la aplicación:
   - crea la base de datos `inmuebles` si no existe,
   - ejecuta `resources/schema.sql` (tablas `propietario`, `inmueble` y `sotano`),
   - ejecuta `resources/data.sql` (5 propietarios predeterminados).

   Los scripts son idempotentes, así que se pueden ejecutar varias veces sin duplicar datos.

> **Nota:** la creación automática asume que la base de datos se llama `inmuebles`. Si usas otro nombre, créala tú antes y ajusta `db.url`.
>
> **Seguridad:** no subas tu contraseña real a un repositorio público. Agrega `resources/application.properties` al `.gitignore` y comparte una copia de ejemplo.

### Modelo de datos

- `propietario(id, nombre, correo_electronico)`
- `inmueble(chip, tipo, fecha_compra, direccion, estado, id_propietario, numero_piso, cantidad_pisos)`: una sola tabla para ambos tipos; la columna `tipo` vale `APARTAMENTO` o `CASA`.
- `sotano(id, chip_casa, area_m2, tipo_uso)`: relación 1 a 0..1 con la casa, con borrado en cascada.

## Ejecución

```bash
mvn javafx:run
```

Para ejecutar las pruebas:

```bash
mvn test
```

Las pruebas del servicio no necesitan base de datos; las de integración JDBC sí necesitan MySQL en ejecución.

## Operaciones CRUD

| Operación | Cómo se hace en la interfaz | Caso de uso |
|---|---|---|
| Crear | Llenar el formulario y pulsar **Guardar** (modo Creación) | `crear` |
| Consultar | Escribir un CHIP y pulsar **Consultar**, o seleccionar una fila de la tabla | `consultarPorChip`, `listar` |
| Actualizar | Cargar un inmueble (el CHIP y el tipo se bloquean), modificar y pulsar **Guardar** (modo Edición) | `actualizar` |
| Eliminar | Seleccionar una fila y pulsar **Eliminar** (pide confirmación) | `eliminar` |
| Limpiar | Pulsar **Limpiar** para vaciar el formulario y volver al modo Creación | (solo interfaz) |

Reglas de negocio: no se permite crear dos inmuebles con el mismo CHIP, y no se puede actualizar ni eliminar uno que no existe.

## Arquitectura

El núcleo (`dominio` y `application`) no conoce JavaFX ni JDBC. Todo lo externo entra o sale por puertos:

- **Puerto de entrada:** `InmuebleUseCase`, implementado por `InmuebleService`.
- **Puertos de salida:** `InmuebleRepository` y `PropietarioRepository`.
- **Adaptador de entrada:** `InmuebleController` (JavaFX).
- **Adaptadores de salida:** `JdbcInmuebleRepository` y `JdbcPropietarioRepository` (MySQL).
- `AppConfig` es el único lugar que conoce las clases concretas y ensambla las dependencias.

Reglas de dependencia: `dominio` no depende de nada; `application` solo depende de `dominio`; `infrastructure` depende de ambos, nunca al revés.

## Estructura del proyecto

```
crud_hexagonal
├── dominio
│   ├── Inmueble.java                     (abstracta, enum Estado)
│   ├── Apartamento.java
│   ├── Casa.java
│   ├── Sotano.java                       (enum TipoUso)
│   ├── Propietario.java
│   └── InmuebleNoEncontradoException.java
├── application
│   ├── port
│   │   ├── InmuebleUseCase.java          (puerto de entrada)
│   │   ├── InmuebleRepository.java       (puerto de salida)
│   │   └── PropietarioRepository.java    (puerto de salida)
│   └── InmuebleService.java
├── infrastructure
│   ├── ui
│   │   └── InmuebleController.java       (adaptador de entrada, JavaFX)
│   ├── persistence
│   │   ├── JdbcInmuebleRepository.java   (adaptador de salida)
│   │   ├── JdbcPropietarioRepository.java
│   │   └── PersistenciaException.java
│   └── config
│       └── AppConfig.java               (conexión y ensamblado)
├── resources
│   ├── inmueble-view.fxml
│   ├── application.properties
│   ├── schema.sql
│   └── data.sql
├── test
│   ├── application                      (pruebas unitarias del servicio)
│   └── infrastructure                   (pruebas de integración JDBC)
├── MainApp.java                         (arranque de JavaFX)
└── pom.xml
```

# Diagramas disponibles en la Wiki



## Documentación del código

Las clases del dominio, los puertos y los adaptadores incluyen Javadoc. Para generarlo:

```bash
mvn javadoc:javadoc
```
