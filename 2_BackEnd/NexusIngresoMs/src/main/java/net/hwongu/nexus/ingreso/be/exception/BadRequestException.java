package net.hwongu.nexus.ingreso.be.exception;

/**
 * Excepcion de negocio para representar errores causados por datos de entrada
 * invalidos enviados por el cliente.
 *
 * @author Henry Wong
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
