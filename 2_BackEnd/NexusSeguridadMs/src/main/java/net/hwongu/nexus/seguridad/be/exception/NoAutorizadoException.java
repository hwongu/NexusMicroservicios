package net.hwongu.nexus.seguridad.be.exception;

/**
 * Excepcion de negocio para representar intentos de autenticacion fallidos.
 *
 * <p>Su objetivo es mantener el mismo manejo centralizado de errores de la
 * API, pero devolviendo semantica HTTP 401 cuando las credenciales no son
 * validas.</p>
 *
 * @author Henry Wong
 */
public class NoAutorizadoException extends RuntimeException {

    public NoAutorizadoException(String message) {
        super(message);
    }
}
