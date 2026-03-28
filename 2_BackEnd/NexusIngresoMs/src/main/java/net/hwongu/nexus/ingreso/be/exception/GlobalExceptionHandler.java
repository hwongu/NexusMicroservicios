package net.hwongu.nexus.ingreso.be.exception;

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
 * @author Henry Wong
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Intercepta errores de validacion de DTOs anotados con Jakarta Validation.
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
     * Intercepta errores de recurso no encontrado.
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
     * Intercepta fallos de integracion con otros microservicios.
     *
     * @param exception excepcion de integracion capturada.
     * @param request peticion HTTP original.
     * @return respuesta HTTP 502 para indicar dependencia remota fallida.
     */
    @ExceptionHandler(IntegracionRemotaException.class)
    public ResponseEntity<ErrorResponseDTO> manejarIntegracionRemota(
            IntegracionRemotaException exception,
            HttpServletRequest request
    ) {
        exception.printStackTrace();
        HttpStatus estadoHttp = HttpStatus.BAD_GATEWAY;
        return construirRespuestaError(estadoHttp, exception.getMessage(), request.getRequestURI());
    }

    /**
     * Intercepta errores de integridad referencial provenientes de Spring Data.
     *
     * @param exception excepcion de integridad capturada por Spring.
     * @param request peticion HTTP original.
     * @return respuesta HTTP 409 para representar un conflicto con el estado actual.
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
     * Ultima barrera defensiva para errores no previstos por reglas especificas.
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
