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
 * Coordina la logica de negocio de usuarios.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAllByOrderByIdUsuarioAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO buscarUsuarioPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
        return convertirADTO(usuario);
    }

    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO usuarioDTO) {
        Usuario usuario = convertirAEntidad(usuarioDTO);
        usuario.setIdUsuario(null);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertirADTO(usuarioGuardado);
    }

    @Transactional
    public void actualizarUsuario(Integer id, UsuarioDTO usuarioDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        usuarioExistente.setUsername(usuarioDTO.getUsername());
        usuarioExistente.setPassword(usuarioDTO.getPassword());
        usuarioExistente.setEstado(usuarioDTO.getEstado());

        usuarioRepository.save(usuarioExistente);
    }

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

    @Transactional(readOnly = true)
    public UsuarioDTO autenticarUsuario(LoginRequestDTO loginRequestDTO) {
        Usuario usuario = usuarioRepository.findByUsernameAndPasswordAndEstadoTrue(
                        loginRequestDTO.getUsername(),
                        loginRequestDTO.getPassword()
                )
                .orElseThrow(() -> new NoAutorizadoException("Credenciales invalidas."));

        return convertirADTO(usuario);
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .estado(usuario.getEstado())
                .build();
    }

    private Usuario convertirAEntidad(UsuarioDTO usuarioDTO) {
        return Usuario.builder()
                .idUsuario(usuarioDTO.getIdUsuario())
                .username(usuarioDTO.getUsername())
                .password(usuarioDTO.getPassword())
                .estado(usuarioDTO.getEstado())
                .build();
    }
}
