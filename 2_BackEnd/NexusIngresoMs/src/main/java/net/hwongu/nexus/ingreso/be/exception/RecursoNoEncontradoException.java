package net.hwongu.nexus.ingreso.be.exception;

/**
 * Excepcion de negocio para indicar que un recurso solicitado no existe.
 *
 * @author Henry Wong
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
