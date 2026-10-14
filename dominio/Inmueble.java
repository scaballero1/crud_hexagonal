package dominio;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad base abstracta del dominio que representa un inmueble.
 * <p>Contiene los atributos comunes a {@link Apartamento} y {@link Casa}. La identidad
 * del inmueble es su CHIP y todas las reglas de validación se aplican en los setters,
 * por lo que un objeto {@code Inmueble} nunca queda en un estado inválido.</p>
 */
public abstract class Inmueble {

    /**
     * Estados posibles de un inmueble: {@code DISPONIBLE}, {@code OCUPADO} o {@code EN_VENTA}.
     */
    public enum Estado {
        DISPONIBLE,
        OCUPADO,
        EN_VENTA
    }

    protected String chip;
    protected LocalDate fechaCompra;
    protected String direccion;
    protected Estado estado;
    protected Propietario propietario;

    /**
     * Crea un inmueble validando todos sus datos.
     *
     * @param chip         identificador catastral único (obligatorio)
     * @param fechaCompra  fecha de compra (obligatoria, no puede ser futura)
     * @param direccion    dirección del inmueble (obligatoria)
     * @param estado       estado actual (obligatorio)
     * @param propietario  propietario del inmueble (obligatorio)
     * @throws IllegalArgumentException si algún dato es nulo, vacío o inválido
     */
    public Inmueble(String chip, LocalDate fechaCompra, String direccion, Estado estado, Propietario propietario) {
        setChip(chip);
        setFechaCompra(fechaCompra);
        setDireccion(direccion);
        setEstado(estado);
        setPropietario(propietario);
    }

    /**
     * @return el valor de chip
     */
    public String getChip() {
        return chip;
    }

    /**
     * Asigna el CHIP eliminando espacios en los extremos.
     *
     * @param chip nuevo CHIP
     * @throws IllegalArgumentException si es nulo o vacío
     */
    public void setChip(String chip) {
        if (chip == null || chip.trim().isEmpty()) {
            throw new IllegalArgumentException("El CHIP del inmueble es obligatorio.");
        }
        this.chip = chip.trim();
    }

    /**
     * @return el valor de fechaCompra
     */
    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    /**
     * Asigna la fecha de compra.
     *
     * @param fechaCompra nueva fecha
     * @throws IllegalArgumentException si es nula o posterior a la fecha actual
     */
    public void setFechaCompra(LocalDate fechaCompra) {
        if (fechaCompra == null) {
            throw new IllegalArgumentException("La fecha de compra es obligatoria.");
        }
        if (fechaCompra.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de compra no puede ser una fecha futura.");
        }
        this.fechaCompra = fechaCompra;
    }

    /**
     * @return el valor de direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * Asigna la dirección eliminando espacios en los extremos.
     *
     * @param direccion nueva dirección
     * @throws IllegalArgumentException si es nula o vacía
     */
    public void setDireccion(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección del inmueble es obligatoria.");
        }
        this.direccion = direccion.trim();
    }

    /**
     * @return el valor de estado
     */
    public Estado getEstado() {
        return estado;
    }

    /**
     * Asigna el estado del inmueble.
     *
     * @param estado nuevo estado
     * @throws IllegalArgumentException si es nulo
     */
    public void setEstado(Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado del inmueble es obligatorio.");
        }
        this.estado = estado;
    }

    /**
     * @return el valor de propietario
     */
    public Propietario getPropietario() {
        return propietario;
    }

    /**
     * Asigna el propietario del inmueble.
     *
     * @param propietario nuevo propietario
     * @throws IllegalArgumentException si es nulo
     */
    public void setPropietario(Propietario propietario) {
        if (propietario == null) {
            throw new IllegalArgumentException("El propietario del inmueble es obligatorio.");
        }
        this.propietario = propietario;
    }

    /**
     * Dos inmuebles son iguales si tienen el mismo CHIP y son del mismo tipo.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inmueble inmueble = (Inmueble) o;
        return Objects.equals(chip, inmueble.chip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(chip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Inmueble{" +
                "chip='" + chip + '\'' +
                ", fechaCompra=" + fechaCompra +
                ", direccion='" + direccion + '\'' +
                ", estado=" + estado +
                ", propietario=" + propietario +
                '}';
    }
}
