package dominio;

import java.util.Objects;

/**
 * Sótano opcional de una {@link Casa}, definido por su área y su tipo de uso.
 */
public class Sotano {

    /**
     * Usos posibles de un sótano: {@code BODEGA}, {@code PARQUEADERO} u {@code OTRO}.
     */
    public enum TipoUso {
        BODEGA,
        PARQUEADERO,
        OTRO
    }

    private double areaM2;
    private TipoUso tipoUso;

    /**
     * Crea un sótano validando sus datos.
     *
     * @param areaM2   área en metros cuadrados (mayor a 0)
     * @param tipoUso  uso del sótano (obligatorio)
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public Sotano(double areaM2, TipoUso tipoUso) {
        setAreaM2(areaM2);
        setTipoUso(tipoUso);
    }

    /**
     * @return el valor de areaM2
     */
    public double getAreaM2() {
        return areaM2;
    }

    /**
     * Asigna el área del sótano.
     *
     * @param areaM2 nueva área en m²
     * @throws IllegalArgumentException si es menor o igual a 0
     */
    public void setAreaM2(double areaM2) {
        if (areaM2 <= 0) {
            throw new IllegalArgumentException("El área del sótano en m2 debe ser mayor a 0.");
        }
        this.areaM2 = areaM2;
    }

    /**
     * @return el valor de tipoUso
     */
    public TipoUso getTipoUso() {
        return tipoUso;
    }

    /**
     * Asigna el tipo de uso del sótano.
     *
     * @param tipoUso nuevo uso
     * @throws IllegalArgumentException si es nulo
     */
    public void setTipoUso(TipoUso tipoUso) {
        if (tipoUso == null) {
            throw new IllegalArgumentException("El tipo de uso del sótano es obligatorio.");
        }
        this.tipoUso = tipoUso;
    }

    /**
     * Dos sótanos son iguales si tienen la misma área y el mismo uso.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sotano sotano = (Sotano) o;
        return Double.compare(sotano.areaM2, areaM2) == 0 && tipoUso == sotano.tipoUso;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(areaM2, tipoUso);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Sotano{" +
                "areaM2=" + areaM2 +
                ", tipoUso=" + tipoUso +
                '}';
    }
}
