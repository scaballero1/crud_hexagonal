package dominio;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Inmueble de tipo apartamento, ubicado en un piso determinado de una edificación.
 */
public class Apartamento extends Inmueble {
    private int numeroPiso;

    /**
     * Crea un apartamento validando sus datos.
     *
     * @param chip         identificador catastral único
     * @param fechaCompra  fecha de compra (no futura)
     * @param direccion    dirección del inmueble
     * @param estado       estado actual
     * @param propietario  propietario del inmueble
     * @param numeroPiso   piso en el que se ubica (mayor a 0)
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public Apartamento(String chip, LocalDate fechaCompra, String direccion, Estado estado, Propietario propietario, int numeroPiso) {
        super(chip, fechaCompra, direccion, estado, propietario);
        setNumeroPiso(numeroPiso);
    }

    /**
     * @return el valor de numeroPiso
     */
    public int getNumeroPiso() {
        return numeroPiso;
    }

    /**
     * Asigna el número de piso.
     *
     * @param numeroPiso nuevo piso
     * @throws IllegalArgumentException si es menor o igual a 0
     */
    public void setNumeroPiso(int numeroPiso) {
        if (numeroPiso <= 0) {
            throw new IllegalArgumentException("El número de piso debe ser mayor a 0.");
        }
        this.numeroPiso = numeroPiso;
    }

    /**
     * Dos apartamentos son iguales si comparten CHIP y número de piso.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Apartamento that = (Apartamento) o;
        return numeroPiso == that.numeroPiso;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numeroPiso);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Apartamento{" +
                "chip='" + chip + '\'' +
                ", fechaCompra=" + fechaCompra +
                ", direccion='" + direccion + '\'' +
                ", estado=" + estado +
                ", propietario=" + propietario +
                ", numeroPiso=" + numeroPiso +
                '}';
    }
}
