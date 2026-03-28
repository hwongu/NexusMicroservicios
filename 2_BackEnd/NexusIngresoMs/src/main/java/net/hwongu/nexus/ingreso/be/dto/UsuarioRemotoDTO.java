package net.hwongu.nexus.ingreso.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de apoyo para leer la respuesta de NexusSeguridadMs.
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRemotoDTO {

    private Integer idUsuario;
    private String username;
    private Boolean estado;
}
