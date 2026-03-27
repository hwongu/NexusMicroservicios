package net.hwongu.nexus.ingreso.be.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO utilizado para transportar los datos de la cabecera de ingreso entre la
 * API REST y la capa de servicio.
 *
 * <p>Se mantiene el identificador del usuario como un dato simple para
 * respetar la separacion entre microservicios: ingresos no persiste ni une
 * entidades del modulo de seguridad.</p>
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngresoDTO {

    /**
     * Identificador del ingreso.
     */
    private Integer idIngreso;

    /**
     * Identificador del usuario que registra el ingreso.
     */
    @NotNull(message = "El id del usuario es obligatorio.")
    @Min(value = 1, message = "El id del usuario debe ser mayor a cero.")
    private Integer idUsuario;

    /**
     * Nombre del usuario para referencia en respuestas.
     */
    private String username;

    /**
     * Fecha y hora del ingreso.
     */
    private LocalDateTime fechaIngreso;

    /**
     * Estado actual del ingreso.
     */
    private String estado;
}
