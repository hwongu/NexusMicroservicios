package net.hwongu.nexus.ingreso.be.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para intercambio de informacion de detalle de ingreso.
 *
 * <p>El identificador de producto se maneja como dato simple porque el
 * producto pertenece a otro microservicio. El nombre del producto se usa solo
 * para enriquecer respuestas cuando catalogo esta disponible.</p>
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleIngresoDTO {

    /**
     * Identificador del detalle.
     */
    private Integer idDetalle;

    /**
     * Identificador del ingreso padre.
     */
    private Integer idIngreso;

    /**
     * Identificador del producto asociado.
     */
    @NotNull(message = "El id del producto es obligatorio.")
    @Min(value = 1, message = "El id del producto debe ser mayor a cero.")
    private Integer idProducto;

    /**
     * Nombre del producto para referencia en respuestas.
     */
    private String nombreProducto;

    /**
     * Cantidad de unidades ingresadas.
     */
    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero.")
    private Integer cantidad;

    /**
     * Precio de compra unitario.
     */
    @NotNull(message = "El precio de compra es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio de compra debe ser mayor a cero.")
    private Double precioCompra;
}
