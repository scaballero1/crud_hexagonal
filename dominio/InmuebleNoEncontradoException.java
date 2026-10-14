package dominio;

/**
 * Excepción no comprobada que indica que no existe un inmueble con el CHIP indicado.
 */
public class InmuebleNoEncontradoException extends RuntimeException {
    /**
     * @param mensaje descripción del error
     */
    public InmuebleNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    /**
     * @param mensaje descripción del error
     * @param causa   excepción original
     */
    public InmuebleNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
