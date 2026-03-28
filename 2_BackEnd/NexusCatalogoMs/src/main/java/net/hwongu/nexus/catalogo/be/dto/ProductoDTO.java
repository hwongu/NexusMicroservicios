package net.hwongu.nexus.catalogo.be.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para intercambio de informacion de productos.
 *
 * <p>Ademas del identificador de categoria, este DTO incluye el nombre de la
 * categoria para las respuestas, manteniendo la misma idea del monolito
 * original, donde el producto se devolvia con informacion enriquecida del
 * {@code JOIN} entre tablas.</p>
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    /**
     * Identificador del producto.
     */
    private Integer idProducto;

    /**
     * Clave foranea hacia la categoria.
     *
     * <p>{@link NotNull} y {@link Min} garantizan que el cliente indique una
     * referencia valida antes de llegar a la capa de persistencia.</p>
     */
    @NotNull(message = "El id de la categoria es obligatorio.")
    @Min(value = 1, message = "El id de la categoria debe ser mayor a cero.")
    private Integer idCategoria;

    /**
     * Nombre de la categoria asociado al producto.
     *
     * <p>Este dato es de solo lectura para la API; se llena en las respuestas
     * para facilitar la visualizacion del catalogo.</p>
     */
    private String nombreCategoria;

    /**
     * Nombre comercial del producto.
     */
    @NotBlank(message = "El nombre del producto es obligatorio.")
    private String nombre;

    /**
     * Precio unitario del producto.
     */
    @NotNull(message = "El precio del producto es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero.")
    private Double precio;

    /**
     * Stock disponible.
     */
    @NotNull(message = "El stock del producto es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    private Integer stock;
}
