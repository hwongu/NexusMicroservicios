package net.hwongu.nexus.ingreso.be.exception;

/**
 * Excepcion de negocio para representar fallos al comunicarse con otros
 * microservicios luego de haber registrado datos locales.
 *
 * @author Henry Wong
 */
public class IntegracionRemotaException extends RuntimeException {

    public IntegracionRemotaException(String message) {
        super(message);
    }
}
