package application.port;

import dominio.Inmueble;
import dominio.Propietario;
import java.util.List;

/**
 * Puerto de entrada: casos de uso disponibles para gestionar inmuebles.
 * <p>Lo implementa {@link application.InmuebleService} y lo consumen los adaptadores
 * de entrada (por ejemplo, la interfaz JavaFX).</p>
 */
public interface InmuebleUseCase {
    /**
     * Registra un nuevo inmueble.
     *
     * @param inmueble inmueble a crear
     * @throws IllegalArgumentException si es nulo o ya existe un inmueble con el mismo CHIP
     */
    void crear(Inmueble inmueble);
    /**
     * Consulta un inmueble por su CHIP.
     *
     * @param chip CHIP del inmueble
     * @return el inmueble encontrado
     * @throws IllegalArgumentException si el CHIP es nulo o vacío
     * @throws dominio.InmuebleNoEncontradoException si no existe
     */
    Inmueble consultarPorChip(String chip);
    /**
     * @return todos los inmuebles registrados, ordenados por CHIP
     */
    List<Inmueble> listar();
    /**
     * Actualiza los datos de un inmueble existente (identificado por su CHIP).
     *
     * @param inmueble inmueble con los datos nuevos
     * @throws IllegalArgumentException si es nulo
     * @throws dominio.InmuebleNoEncontradoException si no existe
     */
    void actualizar(Inmueble inmueble);
    /**
     * Elimina un inmueble.
     *
     * @param chip CHIP del inmueble a eliminar
     * @throws IllegalArgumentException si el CHIP es nulo o vacío
     * @throws dominio.InmuebleNoEncontradoException si no existe
     */
    void eliminar(String chip);
    /**
     * @return los propietarios predeterminados, para seleccionarlos al registrar un inmueble
     */
    List<Propietario> listarPropietarios();
}
