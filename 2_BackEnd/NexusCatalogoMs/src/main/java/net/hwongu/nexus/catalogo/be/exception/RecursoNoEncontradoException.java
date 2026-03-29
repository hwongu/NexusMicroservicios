package net.hwongu.nexus.catalogo.be.exception;

/**
 * Representa errores por recursos inexistentes.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
