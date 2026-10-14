package application.port;

import dominio.Propietario;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: consulta de propietarios (solo lectura).
 */
public interface PropietarioRepository {
    /**
     * @param id identificación del propietario
     * @return el propietario, o un {@code Optional} vacío si no existe
     */
    Optional<Propietario> buscarPorId(int id);
    /**
     * @return todos los propietarios registrados
     */
    List<Propietario> listarTodos();
}
