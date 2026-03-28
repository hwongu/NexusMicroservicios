package net.hwongu.nexus.seguridad.be.exception;

/**
 * Excepcion de negocio para representar errores causados por datos de entrada
 * invalidos enviados por el cliente.
 *
 * <p>Su uso didactico principal es diferenciar claramente dos escenarios:
 * <ul>
 *     <li>El cliente envio una peticion mal formada o inconsistente, que debe
 *     responder con HTTP 400.</li>
 *     <li>El cliente pidio un recurso inexistente por URL, que debe responder
 *     con HTTP 404.</li>
 * </ul>
 * Esta separacion hace que la semantica REST sea mucho mas facil de explicar
 * a los estudiantes.</p>
 *
 * @author Henry Wong
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
