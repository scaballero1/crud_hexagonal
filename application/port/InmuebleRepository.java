package application.port;

import dominio.Inmueble;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: persistencia de inmuebles.
 * <p>Lo define la capa de aplicación y lo implementa un adaptador de infraestructura
 * (por ejemplo, {@link infrastructure.persistence.JdbcInmuebleRepository}).</p>
 */
public interface InmuebleRepository {
    /**
     * Guarda un inmueble nuevo (junto con su sótano, si es una casa que lo tiene).
     *
     * @param inmueble inmueble a guardar
     */
    void guardar(Inmueble inmueble);
    /**
     * @param chip CHIP del inmueble
     * @return el inmueble, o un {@code Optional} vacío si no existe
     */
    Optional<Inmueble> buscarPorChip(String chip);
    /**
     * @return todos los inmuebles almacenados
     */
    List<Inmueble> listarTodos();
    /**
     * Actualiza un inmueble existente.
     *
     * @param inmueble inmueble con los datos nuevos
     */
    void actualizar(Inmueble inmueble);
    /**
     * Elimina un inmueble por su CHIP.
     *
     * @param chip CHIP del inmueble
     */
    void eliminar(String chip);
    /**
     * @param chip CHIP a verificar
     * @return {@code true} si existe un inmueble con ese CHIP
     */
    boolean existePorChip(String chip);
}
