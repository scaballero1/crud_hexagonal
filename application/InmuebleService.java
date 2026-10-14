package application;

import application.port.InmuebleRepository;
import application.port.InmuebleUseCase;
import application.port.PropietarioRepository;
import dominio.Inmueble;
import dominio.InmuebleNoEncontradoException;
import dominio.Propietario;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de aplicación que implementa los casos de uso de {@link InmuebleUseCase}.
 * <p>Contiene las reglas de negocio (CHIP único, existencia previa para actualizar o
 * eliminar) y delega el almacenamiento en los puertos de salida. No depende de JDBC ni
 * de JavaFX.</p>
 */
public class InmuebleService implements InmuebleUseCase {

    private final InmuebleRepository inmuebleRepository;
    private final PropietarioRepository propietarioRepository;

    /**
     * Crea el servicio sin repositorio de propietarios (la lista de propietarios quedará vacía).
     *
     * @param inmuebleRepository puerto de persistencia de inmuebles (obligatorio)
     */
    public InmuebleService(InmuebleRepository inmuebleRepository) {
        this(inmuebleRepository, null);
    }

    /**
     * Crea el servicio con sus dependencias.
     *
     * @param inmuebleRepository    puerto de persistencia de inmuebles (obligatorio)
     * @param propietarioRepository puerto de consulta de propietarios
     */
    public InmuebleService(InmuebleRepository inmuebleRepository, PropietarioRepository propietarioRepository) {
        this.inmuebleRepository = Objects.requireNonNull(inmuebleRepository, "El repositorio de inmuebles no puede ser nulo.");
        this.propietarioRepository = propietarioRepository;
    }

    /**
     * {@inheritDoc}
     * <p>Rechaza el registro si ya existe un inmueble con el mismo CHIP.</p>
     */
    @Override
    public void crear(Inmueble inmueble) {
        if (inmueble == null) {
            throw new IllegalArgumentException("El inmueble no puede ser nulo.");
        }
        if (inmuebleRepository.existePorChip(inmueble.getChip())) {
            throw new IllegalArgumentException("Ya existe un inmueble con el CHIP: " + inmueble.getChip());
        }
        inmuebleRepository.guardar(inmueble);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inmueble consultarPorChip(String chip) {
        if (chip == null || chip.trim().isEmpty()) {
            throw new IllegalArgumentException("El CHIP no puede estar vacío.");
        }
        return inmuebleRepository.buscarPorChip(chip.trim())
                .orElseThrow(() -> new InmuebleNoEncontradoException("No se encontró ningún inmueble con el CHIP: " + chip));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Inmueble> listar() {
        return inmuebleRepository.listarTodos();
    }

    /**
     * {@inheritDoc}
     * <p>Verifica antes que el inmueble exista.</p>
     */
    @Override
    public void actualizar(Inmueble inmueble) {
        if (inmueble == null) {
            throw new IllegalArgumentException("El inmueble no puede ser nulo.");
        }
        if (!inmuebleRepository.existePorChip(inmueble.getChip())) {
            throw new InmuebleNoEncontradoException("No se encontró el inmueble a actualizar con CHIP: " + inmueble.getChip());
        }
        inmuebleRepository.actualizar(inmueble);
    }

    /**
     * {@inheritDoc}
     * <p>Verifica antes que el inmueble exista.</p>
     */
    @Override
    public void eliminar(String chip) {
        if (chip == null || chip.trim().isEmpty()) {
            throw new IllegalArgumentException("El CHIP no puede estar vacío.");
        }
        if (!inmuebleRepository.existePorChip(chip.trim())) {
            throw new InmuebleNoEncontradoException("No se encontró el inmueble a eliminar con CHIP: " + chip);
        }
        inmuebleRepository.eliminar(chip.trim());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Propietario> listarPropietarios() {
        if (propietarioRepository == null) {
            return List.of();
        }
        return propietarioRepository.listarTodos();
    }
}
