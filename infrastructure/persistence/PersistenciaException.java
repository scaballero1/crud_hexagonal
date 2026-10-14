package infrastructure.persistence;

/**
 * Excepción no comprobada que envuelve los errores de base de datos ({@code SQLException}),
 * para que no se filtren al núcleo de la aplicación.
 */
public class PersistenciaException extends RuntimeException {

    /**
     * @param message descripción del error
     */
    public PersistenciaException(String message) {
        super(message);
    }

    /**
     * @param message descripción del error
     * @param cause   excepción original
     */
    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
