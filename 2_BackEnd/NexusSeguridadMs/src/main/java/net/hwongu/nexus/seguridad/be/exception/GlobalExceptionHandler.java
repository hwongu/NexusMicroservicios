package net.hwongu.nexus.seguridad.be.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la API REST del microservicio.
 *
 * <p>La anotacion {@link RestControllerAdvice} le indica a Spring Boot que
 * esta clase debe observar de forma transversal a todos los controladores
 * anotados con {@code @RestController}. Internamente, Spring registra este
 * componente dentro de la infraestructura de Spring MVC y, cuando una
 * excepcion escapa desde un controlador o desde su capa de servicio asociada,
 * el framework busca un metodo {@link ExceptionHandler} compatible con el tipo
 * de error lanzado.</p>
 *
 * <p>Esto permite centralizar la politica de manejo de excepciones en un solo
 * lugar. El beneficio pedagogico es clave: los controladores pueden enfocarse
 * exclusivamente en recibir la peticion y delegar al servicio, mientras que la
 * conversion de errores a respuestas HTTP queda desacoplada y reutilizable.</p>
 *
 * <p>En otras palabras, Spring Boot reemplaza multiples bloques
 * {@code try-catch} repetidos por un mecanismo declarativo de intercepcion,
 * mejorando la legibilidad, la mantenibilidad y la consistencia de la API.</p>
 *
 * @author Henry Wong
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Intercepta errores de validacion de DTOs anotados con Jakarta Validation.
     *
     * <p>Esta excepcion se dispara antes de que el metodo del controlador
     * termine su ejecucion, porque Spring valida automaticamente el cuerpo
     * anotado con {@code @Valid}. Si algun campo incumple reglas como
     * {@code @NotBlank} o {@code @NotNull}, el framework construye un objeto
     * {@link MethodArgumentNotValidException} con el detalle de cada fallo.</p>
     *
     * @param exception excepcion de validacion generada por Spring MVC.
     * @param request peticion HTTP original.
     * @return respuesta HTTP 400 con el detalle por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarErroresDeValidacion(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        exception.printStackTrace();
        Map<String, String> errores = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        HttpStatus estadoHttp = HttpStatus.BAD_REQUEST;
        return construirRespuestaError(estadoHttp, errores, request.getRequestURI());
    }

    /**
     * Intercepta errores de datos invalidos enviados por el cliente.
     *
     * <p>Este escenario complementa las validaciones declarativas. Mientras
     * {@code @Valid} cubre reglas de formato y obligatoriedad, esta excepcion
     * permite reportar reglas de negocio simples, como cuando se envia un
     * valor funcionalmente inconsistente.</p>
     *
     * @param exception excepcion asociada a una solicitud invalida.
     * @param request peticion HTTP original.
     * @return respuesta HTTP 400 con el mensaje funcional exacto.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> manejarBadRequest(
            BadRequestException exception,
            HttpServletRequest request
    ) {
        exception.printStackTrace();
        HttpStatus estadoHttp = HttpStatus.BAD_REQUEST;
        return construirRespuestaError(estadoHttp, exception.getMessage(), request.getRequestURI());
    }

    /**
     * Intercepta errores de autenticacion.
     *
     * @param exception excepcion asociada a credenciales invalidas.
     * @param request peticion HTTP original.
     * @return respuesta HTTP 401 con el mensaje funcional exacto.
     */
    @ExceptionHandler(NoAutorizadoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNoAutorizado(
            NoAutorizadoException exception,
            HttpServletRequest request
    ) {
        exception.printStackTrace();
        HttpStatus estadoHttp = HttpStatus.UNAUTHORIZED;
        return construirRespuestaError(estadoHttp, exception.getMessage(), request.getRequestURI());
    }

    /**
     * Intercepta errores de recurso no encontrado.
     *
     * <p>Esta excepcion representa el caso REST clasico en el que el cliente
     * pide un recurso inexistente por su identificador en la URL.</p>
     *
     * @param exception excepcion asociada a un recurso inexistente.
     * @param request peticion HTTP original.
     * @return respuesta HTTP 404 con el mensaje funcional exacto.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException exception,
            HttpServletRequest request
    ) {
        exception.printStackTrace();
        HttpStatus estadoHttp = HttpStatus.NOT_FOUND;
        return construirRespuestaError(estadoHttp, exception.getMessage(), request.getRequestURI());
    }

    /**
     * Intercepta errores de integridad referencial provenientes de Spring Data.
     *
     * <p>Este escenario aparece tipicamente al intentar eliminar un registro
     * que aun esta siendo referenciado por otra tabla.</p>
     *
     * @param exception excepcion de integridad capturada por Spring.
     * @param request peticion HTTP original.
     * @return respuesta HTTP 409 para representar un conflicto con el estado
     * actual de los datos.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarErroresDeIntegridad(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        HttpStatus estadoHttp = HttpStatus.CONFLICT;
        return construirRespuestaError(estadoHttp, exception.getMessage(), request.getRequestURI());
    }

    /**
     * Ultima barrera defensiva para errores no previstos por reglas
     * especificas.
     *
     * <p>Colocar este manejador al final es una buena practica porque evita
     * que excepciones inesperadas lleguen sin control al cliente. En una
     * aplicacion productiva normalmente tambien se registraria el error en un
     * sistema de logs o monitoreo centralizado.</p>
     *
     * @param exception excepcion generica no controlada.
     * @param request peticion HTTP original.
     * @return respuesta HTTP 500 estandarizada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErrorGenerico(
            Exception exception,
            HttpServletRequest request
    ) {
        exception.printStackTrace();
        HttpStatus estadoHttp = HttpStatus.INTERNAL_SERVER_ERROR;
        String mensaje = "Ocurrio un error interno no controlado en el servidor.";
        return construirRespuestaError(estadoHttp, mensaje, request.getRequestURI());
    }

    /**
     * Metodo de apoyo para construir respuestas de error uniformes.
     *
     * <p>Centralizar la creacion del DTO evita duplicacion y garantiza que toda
     * excepcion convertida a HTTP comparta la misma estructura JSON.</p>
     *
     * @param estadoHttp estado HTTP a devolver.
     * @param mensaje mensaje o detalle funcional del error.
     * @param ruta ruta donde ocurrio la excepcion.
     * @return {@link ResponseEntity} con la estructura estandar del error.
     */
    private ResponseEntity<ErrorResponseDTO> construirRespuestaError(
            HttpStatus estadoHttp,
            Object mensaje,
            String ruta
    ) {
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(estadoHttp.value())
                .error(estadoHttp.getReasonPhrase())
                .message(mensaje)
                .path(ruta)
                .build();

        return ResponseEntity.status(estadoHttp).body(errorResponseDTO);
    }
}
