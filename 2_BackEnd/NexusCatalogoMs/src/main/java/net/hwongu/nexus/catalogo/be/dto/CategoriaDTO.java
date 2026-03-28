package net.hwongu.nexus.catalogo.be.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para transportar los datos de categoria entre la API REST y
 * la capa de servicio.
 *
 * <p>Separamos DTO y entidad para no exponer directamente el modelo de
 * persistencia. Esto es importante en microservicios porque la API debe poder
 * evolucionar de forma controlada sin acoplarse a la estructura interna de
 * JPA.</p>
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaDTO {

    /**
     * Identificador de la categoria.
     *
     * <p>En altas normalmente llega nulo y en actualizaciones se toma desde la
     * URL para evitar inconsistencias entre el cuerpo y la ruta.</p>
     */
    private Integer idCategoria;

    /**
     * Nombre obligatorio de la categoria.
     *
     * <p>{@link NotBlank} se usa para impedir cadenas nulas, vacias o con solo
     * espacios, que en un catalogo real no tienen valor semantico.</p>
     */
    @NotBlank(message = "El nombre de la categoria es obligatorio.")
    private String nombre;

    /**
     * Descripcion informativa de la categoria.
     */
    private String descripcion;
}
