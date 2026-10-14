package infrastructure.persistence;

import application.port.InmuebleRepository;
import dominio.Apartamento;
import dominio.Casa;
import dominio.Inmueble;
import dominio.Propietario;
import dominio.Sotano;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de salida: implementa {@link application.port.InmuebleRepository} con JDBC sobre MySQL.
 * <p>Los inmuebles se guardan en la tabla {@code inmueble} (columna {@code tipo} distingue
 * apartamento de casa) y el sótano en la tabla {@code sotano}. Las operaciones de
 * guardar y actualizar se ejecutan en una transacción con rollback ante errores.</p>
 */
public class JdbcInmuebleRepository implements InmuebleRepository {

    private static final String SELECT_BASE =
            "SELECT i.chip, i.tipo, i.fecha_compra, i.direccion, i.estado, " +
            "       i.numero_piso, i.cantidad_pisos, " +
            "       p.id AS prop_id, p.nombre AS prop_nombre, p.correo_electronico AS prop_correo, " +
            "       s.area_m2 AS sotano_area, s.tipo_uso AS sotano_uso " +
            "FROM inmueble i " +
            "INNER JOIN propietario p ON i.id_propietario = p.id " +
            "LEFT JOIN sotano s ON i.chip = s.chip_casa";

    private final Connection connection;

    /**
     * @param connection conexión JDBC abierta
     */
    public JdbcInmuebleRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "La conexión a la base de datos no puede ser nula.");
    }

    /**
     * Inserta el inmueble y, si es una casa con sótano, también el sótano, en una sola transacción.
     *
     * @throws PersistenciaException si ocurre un error de base de datos
     */
    @Override
    public void guardar(Inmueble inmueble) {
        Objects.requireNonNull(inmueble, "El inmueble no puede ser nulo.");
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);

            String sqlInmueble =
                    "INSERT INTO inmueble (chip, tipo, fecha_compra, direccion, estado, id_propietario, numero_piso, cantidad_pisos) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = connection.prepareStatement(sqlInmueble)) {
                ps.setString(1, inmueble.getChip());
                ps.setDate(3, Date.valueOf(inmueble.getFechaCompra()));
                ps.setString(4, inmueble.getDireccion());
                ps.setString(5, inmueble.getEstado().name());
                ps.setInt(6, inmueble.getPropietario().getId());

                if (inmueble instanceof Apartamento apto) {
                    ps.setString(2, "APARTAMENTO");
                    ps.setInt(7, apto.getNumeroPiso());
                    ps.setNull(8, Types.INTEGER);
                } else if (inmueble instanceof Casa casa) {
                    ps.setString(2, "CASA");
                    ps.setNull(7, Types.INTEGER);
                    ps.setInt(8, casa.getCantidadPisos());
                } else {
                    throw new IllegalArgumentException("Tipo de inmueble no soportado: " + inmueble.getClass().getName());
                }

                ps.executeUpdate();
            }

            if (inmueble instanceof Casa casa && casa.getSotano().isPresent()) {
                Sotano sotano = casa.getSotano().get();
                String sqlSotano = "INSERT INTO sotano (chip_casa, area_m2, tipo_uso) VALUES (?, ?, ?)";
                try (PreparedStatement psSotano = connection.prepareStatement(sqlSotano)) {
                    psSotano.setString(1, casa.getChip());
                    psSotano.setDouble(2, sotano.getAreaM2());
                    psSotano.setString(3, sotano.getTipoUso().name());
                    psSotano.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error al guardar el inmueble con CHIP: " + inmueble.getChip(), e);
        } finally {
            restaurarAutoCommit(autoCommitOriginal);
        }
    }

    /**
     * {@inheritDoc}
     * @throws PersistenciaException si ocurre un error de base de datos
     */
    @Override
    public Optional<Inmueble> buscarPorChip(String chip) {
        String sql = SELECT_BASE + " WHERE i.chip = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chip);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearInmueble(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar inmueble con CHIP: " + chip, e);
        }
    }

    /**
     * {@inheritDoc}
     * @throws PersistenciaException si ocurre un error de base de datos
     */
    @Override
    public List<Inmueble> listarTodos() {
        String sql = SELECT_BASE + " ORDER BY i.chip ASC";
        List<Inmueble> inmuebles = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                inmuebles.add(mapearInmueble(rs));
            }
            return inmuebles;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar los inmuebles.", e);
        }
    }

    /**
     * Actualiza el inmueble y reemplaza su sótano (lo borra y lo vuelve a insertar si aplica),
     * en una sola transacción.
     *
     * @throws PersistenciaException si ocurre un error de base de datos
     */
    @Override
    public void actualizar(Inmueble inmueble) {
        Objects.requireNonNull(inmueble, "El inmueble no puede ser nulo.");
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = connection.getAutoCommit();
            connection.setAutoCommit(false);

            String sqlInmueble =
                    "UPDATE inmueble SET tipo = ?, fecha_compra = ?, direccion = ?, estado = ?, " +
                    "       id_propietario = ?, numero_piso = ?, cantidad_pisos = ? " +
                    "WHERE chip = ?";

            try (PreparedStatement ps = connection.prepareStatement(sqlInmueble)) {
                ps.setDate(2, Date.valueOf(inmueble.getFechaCompra()));
                ps.setString(3, inmueble.getDireccion());
                ps.setString(4, inmueble.getEstado().name());
                ps.setInt(5, inmueble.getPropietario().getId());
                ps.setString(8, inmueble.getChip());

                if (inmueble instanceof Apartamento apto) {
                    ps.setString(1, "APARTAMENTO");
                    ps.setInt(6, apto.getNumeroPiso());
                    ps.setNull(7, Types.INTEGER);
                } else if (inmueble instanceof Casa casa) {
                    ps.setString(1, "CASA");
                    ps.setNull(6, Types.INTEGER);
                    ps.setInt(7, casa.getCantidadPisos());
                } else {
                    throw new IllegalArgumentException("Tipo de inmueble no soportado: " + inmueble.getClass().getName());
                }

                ps.executeUpdate();
            }

            // Borrar sótano previo si existía
            String sqlDeleteSotano = "DELETE FROM sotano WHERE chip_casa = ?";
            try (PreparedStatement psDel = connection.prepareStatement(sqlDeleteSotano)) {
                psDel.setString(1, inmueble.getChip());
                psDel.executeUpdate();
            }

            // Si es casa y tiene sótano, guardar el nuevo sótano en la misma transacción
            if (inmueble instanceof Casa casa && casa.getSotano().isPresent()) {
                Sotano sotano = casa.getSotano().get();
                String sqlInsertSotano = "INSERT INTO sotano (chip_casa, area_m2, tipo_uso) VALUES (?, ?, ?)";
                try (PreparedStatement psSotano = connection.prepareStatement(sqlInsertSotano)) {
                    psSotano.setString(1, casa.getChip());
                    psSotano.setDouble(2, sotano.getAreaM2());
                    psSotano.setString(3, sotano.getTipoUso().name());
                    psSotano.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            hacerRollback();
            throw new PersistenciaException("Error al actualizar inmueble con CHIP: " + inmueble.getChip(), e);
        } finally {
            restaurarAutoCommit(autoCommitOriginal);
        }
    }

    /**
     * Elimina el inmueble; su sótano se borra en cascada por la clave foránea.
     *
     * @throws PersistenciaException si ocurre un error de base de datos
     */
    @Override
    public void eliminar(String chip) {
        String sql = "DELETE FROM inmueble WHERE chip = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chip);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar inmueble con CHIP: " + chip, e);
        }
    }

    /**
     * {@inheritDoc}
     * @throws PersistenciaException si ocurre un error de base de datos
     */
    @Override
    public boolean existePorChip(String chip) {
        String sql = "SELECT 1 FROM inmueble WHERE chip = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chip);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar existencia de CHIP: " + chip, e);
        }
    }

    /**
     * Convierte una fila del resultado en un {@link Apartamento} o una {@link Casa}
     * según la columna {@code tipo}.
     */
    private Inmueble mapearInmueble(ResultSet rs) throws SQLException {
        String chip = rs.getString("chip");
        String tipo = rs.getString("tipo");
        LocalDate fechaCompra = rs.getDate("fecha_compra").toLocalDate();
        String direccion = rs.getString("direccion");
        Inmueble.Estado estado = Inmueble.Estado.valueOf(rs.getString("estado"));

        int propId = rs.getInt("prop_id");
        String propNombre = rs.getString("prop_nombre");
        String propCorreo = rs.getString("prop_correo");
        Propietario propietario = new Propietario(propId, propNombre, propCorreo);

        if ("APARTAMENTO".equalsIgnoreCase(tipo)) {
            int numeroPiso = rs.getInt("numero_piso");
            return new Apartamento(chip, fechaCompra, direccion, estado, propietario, numeroPiso);
        } else if ("CASA".equalsIgnoreCase(tipo)) {
            int cantidadPisos = rs.getInt("cantidad_pisos");
            Sotano sotano = null;
            double areaM2 = rs.getDouble("sotano_area");
            if (!rs.wasNull()) {
                String tipoUsoStr = rs.getString("sotano_uso");
                Sotano.TipoUso tipoUso = Sotano.TipoUso.valueOf(tipoUsoStr);
                sotano = new Sotano(areaM2, tipoUso);
            }
            return new Casa(chip, fechaCompra, direccion, estado, propietario, cantidadPisos, sotano);
        } else {
            throw new IllegalStateException("Tipo de inmueble no reconocido en la base de datos: " + tipo);
        }
    }

    /**
     * Deshace la transacción en curso; ignora los errores del propio rollback.
     */
    private void hacerRollback() {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            // Se suprime para no enmascarar la excepción original
        }
    }

    /**
     * Devuelve la conexión a su modo de confirmación automática original.
     */
    private void restaurarAutoCommit(boolean original) {
        try {
            connection.setAutoCommit(original);
        } catch (SQLException ex) {
            // Ignorar al restaurar
        }
    }
}
