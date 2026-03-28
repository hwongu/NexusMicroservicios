package net.hwongu.nexus.seguridad.be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hwongu.nexus.seguridad.be.dto.LoginRequestDTO;
import net.hwongu.nexus.seguridad.be.dto.UsuarioDTO;
import net.hwongu.nexus.seguridad.be.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del recurso usuarios.
 *
 * <p>Este controlador traslada la logica HTTP del antiguo modulo de seguridad
 * a Spring MVC, aprovechando anotaciones declarativas como {@link GetMapping}
 * o {@link PostMapping}, que hacen el codigo mas expresivo y mantenible para
 * los estudiantes.</p>
 *
 * @author Henry Wong
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Lista todos los usuarios del sistema.
     *
     * @return lista de usuarios sin exponer la contrasena.
     */
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador solicitado.
     * @return usuario encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorId(id));
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param usuarioDTO datos enviados por el cliente.
     * @return usuario creado con HTTP 201.
     */
    @PostMapping
    public ResponseEntity<UsuarioDTO> registrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO usuarioCreado = usuarioService.registrarUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param id identificador del usuario.
     * @param usuarioDTO nuevos datos del usuario.
     * @return mensaje de exito.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioDTO usuarioDTO
    ) {
        usuarioService.actualizarUsuario(id, usuarioDTO);
        return ResponseEntity.ok(Map.of("message", "Usuario actualizado exitosamente"));
    }

    /**
     * Elimina un usuario por su identificador.
     *
     * @param id identificador del usuario.
     * @return 204 si la operacion se realiza.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Valida las credenciales de un usuario activo.
     *
     * @param loginRequestDTO credenciales enviadas desde el cliente.
     * @return usuario autenticado sin exponer la contrasena.
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(usuarioService.autenticarUsuario(loginRequestDTO));
    }
}
