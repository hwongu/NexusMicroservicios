package net.hwongu.nexus.catalogo.be.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simple para transportar una solicitud de ajuste de stock.
 *
 * <p>Se usa en la integracion con el microservicio de ingresos, que necesita
 * indicar cuanto stock sumar o restar sin reenviar el producto completo.</p>
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarStockRequestDTO {

    /**
     * Cantidad de unidades a modificar en el stock.
     */
    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero.")
    private Integer cantidad;

    /**
     * Tipo de operacion solicitada sobre el stock.
     */
    @NotBlank(message = "La operacion es obligatoria.")
    private String operacion;
}
