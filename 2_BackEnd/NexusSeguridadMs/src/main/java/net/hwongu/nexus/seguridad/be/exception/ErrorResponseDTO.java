package net.hwongu.nexus.seguridad.be.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO estandarizado para representar errores devueltos por la API REST.
 *
 * <p>En un microservicio real, un formato de error consistente simplifica el
 * consumo por parte de clientes frontend, aplicaciones moviles y otros
 * microservicios. Desde la perspectiva docente, este objeto permite mostrar a
 * los estudiantes que una API profesional no devuelve mensajes improvisados,
 * sino una estructura predecible y facil de interpretar.</p>
 *
 * <p>El campo {@code message} se declara como {@link Object} para soportar dos
 * escenarios comunes:
 * <ul>
 *     <li>Un {@link String} cuando se desea informar un mensaje puntual.</li>
 *     <li>Un {@code Map<String, String>} cuando se reportan errores de
 *     validacion por campo.</li>
 * </ul>
 * De este modo, un solo DTO cubre tanto errores funcionales como errores de
 * entrada de datos.</p>
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {

    /**
     * Fecha y hora exacta en la que se construyo la respuesta de error.
     *
     * <p>Este dato es util para auditoria, trazabilidad y correlacion con logs
     * del servidor.</p>
     */
    private LocalDateTime timestamp;

    /**
     * Codigo HTTP numerico devuelto al cliente.
     */
    private int status;

    /**
     * Descripcion corta estandar del error HTTP, por ejemplo
     * {@code BAD_REQUEST} o {@code NOT_FOUND}.
     */
    private String error;

    /**
     * Mensaje funcional para el cliente o mapa de errores de validacion.
     */
    private Object message;

    /**
     * Ruta del endpoint en la cual se produjo la excepcion.
     */
    private String path;
}
