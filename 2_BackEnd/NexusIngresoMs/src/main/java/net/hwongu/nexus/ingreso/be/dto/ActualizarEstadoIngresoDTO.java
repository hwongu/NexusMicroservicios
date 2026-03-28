package net.hwongu.nexus.ingreso.be.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simple para actualizar el estado de un ingreso.
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoIngresoDTO {

    /**
     * Nuevo estado solicitado para el ingreso.
     */
    @NotBlank(message = "El estado es obligatorio.")
    private String estado;
}
