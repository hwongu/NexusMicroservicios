package net.hwongu.nexus.ingreso.be.exception;

/**
 * Representa errores de solicitud invalida.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
