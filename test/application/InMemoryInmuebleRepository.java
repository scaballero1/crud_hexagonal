package application;

import application.port.InmuebleRepository;
import dominio.Inmueble;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio en memoria temporal únicamente para pruebas unitarias.
 */
public class InMemoryInmuebleRepository implements InmuebleRepository {

    private final Map<String, Inmueble> data = new LinkedHashMap<>();

    @Override
    public void guardar(Inmueble inmueble) {
        if (inmueble != null && inmueble.getChip() != null) {
            data.put(inmueble.getChip(), inmueble);
        }
    }

    @Override
    public Optional<Inmueble> buscarPorChip(String chip) {
        if (chip == null) return Optional.empty();
        return Optional.ofNullable(data.get(chip));
    }

    @Override
    public List<Inmueble> listarTodos() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void actualizar(Inmueble inmueble) {
        if (inmueble != null && inmueble.getChip() != null) {
            data.put(inmueble.getChip(), inmueble);
        }
    }

    @Override
    public void eliminar(String chip) {
        if (chip != null) {
            data.remove(chip);
        }
    }

    @Override
    public boolean existePorChip(String chip) {
        if (chip == null) return false;
        return data.containsKey(chip);
    }
}
