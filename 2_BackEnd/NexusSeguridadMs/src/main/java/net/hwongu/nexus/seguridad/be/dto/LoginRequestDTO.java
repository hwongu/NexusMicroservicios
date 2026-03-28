package net.hwongu.nexus.seguridad.be.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO especifico para el proceso de autenticacion.
 *
 * <p>Se separa del DTO general del usuario para mantener el contrato del
 * endpoint de login centrado solo en las credenciales necesarias.</p>
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    /**
     * Nombre de usuario usado para autenticarse.
     */
    @NotBlank(message = "El username es obligatorio.")
    private String username;

    /**
     * Contrasena del usuario.
     */
    @NotBlank(message = "El password es obligatorio.")
    private String password;
}
