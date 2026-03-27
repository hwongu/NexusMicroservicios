package net.hwongu.nexus.ingreso.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simple para solicitar ajustes de stock al microservicio de catalogo.
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarStockRequestDTO {

    private Integer cantidad;
    private String operacion;
}
