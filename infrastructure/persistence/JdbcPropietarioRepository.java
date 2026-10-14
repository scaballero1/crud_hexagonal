package infrastructure.persistence;

import application.port.PropietarioRepository;
import dominio.Propietario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de salida: implementa {@link application.port.PropietarioRepository} con JDBC (solo lectura).
 */
public class JdbcPropietarioRepository implements PropietarioRepository {

    private final Connection connection;

    /**
     * @param connection conexión JDBC abierta
     */
    public JdbcPropietarioRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "La conexión a la base de datos no puede ser nula.");
    }

    /**
     * {@inheritDoc}
     * @throws PersistenciaException si ocurre un error de base de datos
     */
    @Override
    public Optional<Propietario> buscarPorId(int id) {
        String sql = "SELECT id, nombre, correo_electronico FROM propietario WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPropietario(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar propietario por ID: " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     * @throws PersistenciaException si ocurre un error de base de datos
     */
    @Override
    public List<Propietario> listarTodos() {
        String sql = "SELECT id, nombre, correo_electronico FROM propietario ORDER BY id ASC";
        List<Propietario> propietarios = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                propietarios.add(mapearPropietario(rs));
            }
            return propietarios;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar todos los propietarios.", e);
        }
    }

    /**
     * Convierte una fila del resultado en un {@link Propietario}.
     */
    private Propietario mapearPropietario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String correoElectronico = rs.getString("correo_electronico");
        return new Propietario(id, nombre, correoElectronico);
    }
}
