package dominio;

import java.util.Objects;

/**
 * Persona propietaria de uno o varios inmuebles.
 * <p>Los propietarios vienen predeterminados en la base de datos; la aplicación
 * no ofrece operaciones para crearlos ni modificarlos.</p>
 */
public class Propietario {
    private int id;
    private String nombre;
    private String correoElectronico;

    /**
     * Crea un propietario validando sus datos.
     *
     * @param id                 identificación del propietario
     * @param nombre             nombre completo (obligatorio)
     * @param correoElectronico  correo electrónico (obligatorio)
     * @throws IllegalArgumentException si el nombre o el correo son nulos o vacíos
     */
    public Propietario(int id, String nombre, String correoElectronico) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del propietario es obligatorio.");
        }
        if (correoElectronico == null || correoElectronico.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico del propietario es obligatorio.");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.correoElectronico = correoElectronico.trim();
    }

    /**
     * @return el valor de id
     */
    public int getId() {
        return id;
    }

    /**
     * Asigna la identificación.
     *
     * @param id nueva identificación
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return la identificación del propietario (alias de {@link #getId()})
     */
    public int getIdentificacion() {
        return id;
    }

    /**
     * @return el valor de nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Asigna el nombre eliminando espacios en los extremos.
     *
     * @param nombre nuevo nombre
     * @throws IllegalArgumentException si es nulo o vacío
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del propietario es obligatorio.");
        }
        this.nombre = nombre.trim();
    }

    /**
     * @return el valor de correoElectronico
     */
    public String getCorreoElectronico() {
        return correoElectronico;
    }

    /**
     * Asigna el correo electrónico eliminando espacios en los extremos.
     *
     * @param correoElectronico nuevo correo
     * @throws IllegalArgumentException si es nulo o vacío
     */
    public void setCorreoElectronico(String correoElectronico) {
        if (correoElectronico == null || correoElectronico.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico del propietario es obligatorio.");
        }
        this.correoElectronico = correoElectronico.trim();
    }

    /**
     * @return el correo electrónico (alias de {@link #getCorreoElectronico()})
     */
    public String getCorreo() {
        return correoElectronico;
    }

    /**
     * Dos propietarios son iguales si tienen la misma identificación.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Propietario that = (Propietario) o;
        return id == that.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * @return el nombre y la identificación, útil para mostrarlo en la interfaz
     */
    @Override
    public String toString() {
        return nombre + " (ID: " + id + ")";
    }
}
