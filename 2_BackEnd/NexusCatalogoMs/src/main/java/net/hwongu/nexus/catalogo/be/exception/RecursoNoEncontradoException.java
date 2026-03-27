package net.hwongu.nexus.catalogo.be.exception;

/**
 * Excepcion de negocio para indicar que un recurso solicitado no existe.
 *
 * <p>Se prefiere una excepcion propia del dominio sobre una excepcion generica
 * de JPA porque su nombre comunica mejor la intencion del sistema y resulta
 * mas natural para explicarlo en clase.</p>
 *
 * @author Henry Wong
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
