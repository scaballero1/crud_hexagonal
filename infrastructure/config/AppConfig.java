package infrastructure.config;

import application.InmuebleService;
import application.port.InmuebleRepository;
import application.port.InmuebleUseCase;
import application.port.PropietarioRepository;
import infrastructure.persistence.JdbcInmuebleRepository;
import infrastructure.persistence.JdbcPropietarioRepository;
import infrastructure.persistence.PersistenciaException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Clase de configuración responsable de leer la configuración,
 * gestionar la conexión a la base de datos y ensamblar las dependencias
 * de la arquitectura hexagonal.
 */
public class AppConfig {

    private static Connection connection;
    private static InmuebleUseCase inmuebleService;

    /**
     * Devuelve el servicio de inmuebles, ensamblándolo la primera vez (patrón singleton).
     *
     * @return el caso de uso ya conectado a sus repositorios JDBC
     */
    public static synchronized InmuebleUseCase getInmuebleService() {
        if (inmuebleService == null) {
            inmuebleService = ensamblar();
        }
        return inmuebleService;
    }

    /**
     * Alias de {@link #getInmuebleService()}.
     *
     * @return el caso de uso de inmuebles
     */
    public static synchronized InmuebleUseCase getInmuebleUseCase() {
        return getInmuebleService();
    }

    /**
     * @return la conexión JDBC, abriéndola si aún no existe
     */
    public static synchronized Connection getConnection() {
        if (connection == null) {
            conectarBaseDeDatos();
        }
        return connection;
    }

    /**
     * Conecta con la base de datos, crea las tablas y construye el servicio con sus adaptadores.
     */
    private static InmuebleUseCase ensamblar() {
        conectarBaseDeDatos();
        inicializarTablas();

        PropietarioRepository propietarioRepository = new JdbcPropietarioRepository(connection);
        InmuebleRepository inmuebleRepository = new JdbcInmuebleRepository(connection);

        return new InmuebleService(inmuebleRepository, propietarioRepository);
    }

    /**
     * Abre la conexión leyendo {@code application.properties}; si la base de datos no existe, la crea.
     *
     * @throws PersistenciaException si no se logra conectar
     */
    private static void conectarBaseDeDatos() {
        Properties props = cargarPropiedades();
        String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/inmuebles");
        String user = props.getProperty("db.user", "root");
        String pass = props.getProperty("db.password", "1234");

        try {
            // Intentar conectar directamente a la base de datos
            try {
                connection = DriverManager.getConnection(url, user, pass);
            } catch (SQLException e) {
                // Si la base de datos no existe aún (error 1049 en MySQL), conectarse al servidor y crearla
                if (e.getErrorCode() == 1049 || (e.getMessage() != null && e.getMessage().contains("Unknown database"))) {
                    String urlServidor = url.substring(0, url.lastIndexOf('/')) + "/";
                    try (Connection connServer = DriverManager.getConnection(urlServidor, user, pass);
                         Statement stmt = connServer.createStatement()) {
                        stmt.execute("CREATE DATABASE IF NOT EXISTS inmuebles;");
                    }
                    connection = DriverManager.getConnection(url, user, pass);
                } else {
                    throw e;
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al conectar con la base de datos: " + url, e);
        }
    }

    /**
     * Ejecuta {@code schema.sql} y {@code data.sql}.
     */
    private static void inicializarTablas() {
        ejecutarScriptSql("/schema.sql", "resources/schema.sql");
        ejecutarScriptSql("/data.sql", "resources/data.sql");
    }

    /**
     * Ejecuta las sentencias de un script SQL separadas por punto y coma.
     *
     * @param resourcePath ruta del script en el classpath
     * @param filePath     ruta alternativa en el sistema de archivos
     */
    private static void ejecutarScriptSql(String resourcePath, String filePath) {
        String sql = leerArchivo(resourcePath, filePath);
        if (sql == null || sql.trim().isEmpty()) {
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            // Separar comandos por punto y coma (ignorando líneas vacías)
            String[] comandos = sql.split(";");
            for (String comando : comandos) {
                String cmdTrim = comando.trim();
                if (!cmdTrim.isEmpty()) {
                    stmt.execute(cmdTrim);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al ejecutar script SQL (" + resourcePath + "): " + e.getMessage(), e);
        }
    }

    /**
     * Carga {@code application.properties} (classpath o carpeta {@code resources}); si no lo encuentra, usa valores por defecto.
     */
    private static Properties cargarPropiedades() {
        Properties props = new Properties();

        // 1. Intentar desde classpath
        try (InputStream is = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                props.load(is);
                return props;
            }
        } catch (Exception ignored) {
        }

        // 2. Intentar desde ruta local en resources/application.properties
        try {
            Path path = Path.of("resources", "application.properties");
            if (Files.exists(path)) {
                try (InputStream is = Files.newInputStream(path)) {
                    props.load(is);
                    return props;
                }
            }
        } catch (Exception ignored) {
        }

        // Valores por defecto
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/inmuebles");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "1234");
        return props;
    }

    /**
     * Lee un archivo de texto del classpath o, si no está, del sistema de archivos.
     *
     * @return el contenido, o {@code null} si no se encuentra
     */
    private static String leerArchivo(String resourcePath, String filePath) {
        // Intentar leer desde classpath
        try (InputStream is = AppConfig.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {
        }

        // Intentar leer desde archivo
        try {
            Path path = Path.of(filePath);
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
