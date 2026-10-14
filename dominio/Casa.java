package dominio;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Inmueble de tipo casa.
 * <p>Tiene una cantidad de pisos y, opcionalmente, un {@link Sotano}. El sótano forma
 * parte de la casa (composición): no existe de forma independiente.</p>
 */
public class Casa extends Inmueble {
    private int cantidadPisos;
    private Sotano sotano;

    /**
     * Crea una casa sin sótano.
     *
     * @param chip           identificador catastral único
     * @param fechaCompra    fecha de compra (no futura)
     * @param direccion      dirección del inmueble
     * @param estado         estado actual
     * @param propietario    propietario del inmueble
     * @param cantidadPisos  cantidad de pisos (mayor a 0)
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public Casa(String chip, LocalDate fechaCompra, String direccion, Estado estado, Propietario propietario, int cantidadPisos) {
        this(chip, fechaCompra, direccion, estado, propietario, cantidadPisos, null);
    }

    /**
     * Crea una casa con sótano opcional.
     *
     * @param chip           identificador catastral único
     * @param fechaCompra    fecha de compra (no futura)
     * @param direccion      dirección del inmueble
     * @param estado         estado actual
     * @param propietario    propietario del inmueble
     * @param cantidadPisos  cantidad de pisos (mayor a 0)
     * @param sotano         sótano de la casa, o {@code null} si no tiene
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public Casa(String chip, LocalDate fechaCompra, String direccion, Estado estado, Propietario propietario, int cantidadPisos, Sotano sotano) {
        super(chip, fechaCompra, direccion, estado, propietario);
        setCantidadPisos(cantidadPisos);
        this.sotano = sotano;
    }

    /**
     * @return el valor de cantidadPisos
     */
    public int getCantidadPisos() {
        return cantidadPisos;
    }

    /**
     * Asigna la cantidad de pisos.
     *
     * @param cantidadPisos nueva cantidad
     * @throws IllegalArgumentException si es menor o igual a 0
     */
    public void setCantidadPisos(int cantidadPisos) {
        if (cantidadPisos <= 0) {
            throw new IllegalArgumentException("La cantidad de pisos debe ser mayor a 0.");
        }
        this.cantidadPisos = cantidadPisos;
    }

    /**
     * @return el sótano de la casa, o un {@code Optional} vacío si no tiene
     */
    public Optional<Sotano> getSotano() {
        return Optional.ofNullable(sotano);
    }

    /**
     * Asigna o quita el sótano.
     *
     * @param sotano nuevo sótano, o {@code null} para indicar que la casa no tiene
     */
    public void setSotano(Sotano sotano) {
        this.sotano = sotano;
    }

    /**
     * Dos casas son iguales si comparten CHIP, cantidad de pisos y sótano.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Casa casa = (Casa) o;
        return cantidadPisos == casa.cantidadPisos && Objects.equals(sotano, casa.sotano);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cantidadPisos, sotano);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Casa{" +
                "chip='" + chip + '\'' +
                ", fechaCompra=" + fechaCompra +
                ", direccion='" + direccion + '\'' +
                ", estado=" + estado +
                ", propietario=" + propietario +
                ", cantidadPisos=" + cantidadPisos +
                ", sotano=" + sotano +
                '}';
    }
}
