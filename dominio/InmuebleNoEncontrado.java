package dominio;

/**
 * Especialización de {@link InmuebleNoEncontradoException} (equivalente a ella).
 */
public class InmuebleNoEncontrado extends InmuebleNoEncontradoException {
    /**
     * @param mensaje descripción del error
     */
    public InmuebleNoEncontrado(String mensaje) {
        super(mensaje);
    }

    /**
     * @param mensaje descripción del error
     * @param causa   excepción original
     */
    public InmuebleNoEncontrado(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
