package net.hwongu.nexus.seguridad.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO utilizado para transportar los datos de usuario entre la API REST y
 * la capa de servicio.
 *
 * <p>Separamos DTO y entidad para no exponer directamente el modelo de
 * persistencia. En este caso, ademas, se marca la contrasena como de solo
 * escritura para permitir altas y actualizaciones sin devolverla nunca al
 * cliente.</p>
 *
 * @author Henry Wong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    /**
     * Identificador del usuario.
     *
     * <p>En altas normalmente llega nulo y en actualizaciones se toma desde la
     * URL para evitar inconsistencias entre el cuerpo y la ruta.</p>
     */
    private Integer idUsuario;

    /**
     * Nombre de usuario obligatorio para operar el sistema.
     */
    @NotBlank(message = "El username es obligatorio.")
    private String username;

    /**
     * Contrasena del usuario.
     *
     * <p>Se permite recibirla en solicitudes, pero no se expone en respuestas
     * JSON del API.</p>
     */
    @NotBlank(message = "El password es obligatorio.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * Estado del usuario dentro del sistema.
     */
    @NotNull(message = "El estado del usuario es obligatorio.")
    private Boolean estado;
}
