package net.hwongu.nexus.seguridad.be.service;

import lombok.RequiredArgsConstructor;
import net.hwongu.nexus.seguridad.be.dto.LoginRequestDTO;
import net.hwongu.nexus.seguridad.be.dto.UsuarioDTO;
import net.hwongu.nexus.seguridad.be.entity.Usuario;
import net.hwongu.nexus.seguridad.be.exception.NoAutorizadoException;
import net.hwongu.nexus.seguridad.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.seguridad.be.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Capa de servicio para operaciones de usuarios.
 *
 * <p>La anotacion {@link Service} marca esta clase como un componente de la
 * capa de negocio. Aqui se centralizan las validaciones simples, las
 * conversiones entre DTO y entidad, y la coordinacion de operaciones
 * transaccionales.</p>
 *
 * @author Henry Wong
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Lista todos los usuarios en el orden del sistema anterior.
     *
     * @return usuarios convertidos a DTO.
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAllByOrderByIdUsuarioAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    /**
     * Busca un usuario por ID.
     *
     * @param id identificador a buscar.
     * @return DTO del usuario encontrado.
     */
    @Transactional(readOnly = true)
    public UsuarioDTO buscarUsuarioPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        return convertirADTO(usuario);
    }

    /**
     * Registra un nuevo usuario.
     *
     * @param usuarioDTO datos recibidos desde la API.
     * @return usuario creado con su ID generado.
     */
    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO usuarioDTO) {
        Usuario usuario = convertirAEntidad(usuarioDTO);
        usuario.setIdUsuario(null);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertirADTO(usuarioGuardado);
    }

    /**
     * Actualiza un usuario existente.
     *
     * <p>Antes de guardar, se verifica que el registro exista para no responder
     * exitosamente sobre un recurso inexistente.</p>
     *
     * @param id identificador del usuario a modificar.
     * @param usuarioDTO nuevos datos del usuario.
     */
    @Transactional
    public void actualizarUsuario(Integer id, UsuarioDTO usuarioDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        usuarioExistente.setUsername(usuarioDTO.getUsername());
        usuarioExistente.setPassword(usuarioDTO.getPassword());
        usuarioExistente.setEstado(usuarioDTO.getEstado());

        usuarioRepository.save(usuarioExistente);
    }

    /**
     * Elimina un usuario si existe y si no tiene dependencias que violen la
     * integridad referencial.
     *
     * @param id identificador del usuario.
     */
    @Transactional
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado.");
        }

        try {
            usuarioRepository.deleteById(id);
            usuarioRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DataIntegrityViolationException(
                    "No se puede eliminar el usuario porque tiene ingresos registrados a su nombre.",
                    exception
            );
        }
    }

    /**
     * Autentica un usuario activo por sus credenciales.
     *
     * @param loginRequestDTO credenciales enviadas por el cliente.
     * @return usuario autenticado sin exponer su contrasena.
     */
    @Transactional(readOnly = true)
    public UsuarioDTO autenticarUsuario(LoginRequestDTO loginRequestDTO) {
        Usuario usuario = usuarioRepository.findByUsernameAndPasswordAndEstadoTrue(
                        loginRequestDTO.getUsername(),
                        loginRequestDTO.getPassword()
                )
                .orElseThrow(() -> new NoAutorizadoException("Credenciales invalidas."));

        return convertirADTO(usuario);
    }

    /**
     * Convierte una entidad a DTO.
     *
     * @param usuario entidad JPA.
     * @return DTO listo para respuesta.
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .estado(usuario.getEstado())
                .build();
    }

    /**
     * Convierte un DTO a entidad.
     *
     * @param usuarioDTO datos recibidos desde cliente o capas superiores.
     * @return entidad lista para persistir.
     */
    private Usuario convertirAEntidad(UsuarioDTO usuarioDTO) {
        return Usuario.builder()
                .idUsuario(usuarioDTO.getIdUsuario())
                .username(usuarioDTO.getUsername())
                .password(usuarioDTO.getPassword())
                .estado(usuarioDTO.getEstado())
                .build();
    }
}
