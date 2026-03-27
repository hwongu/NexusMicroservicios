package net.hwongu.nexus.seguridad.be.service;

import net.hwongu.nexus.seguridad.be.dto.LoginRequestDTO;
import net.hwongu.nexus.seguridad.be.dto.UsuarioDTO;
import net.hwongu.nexus.seguridad.be.entity.Usuario;
import net.hwongu.nexus.seguridad.be.exception.NoAutorizadoException;
import net.hwongu.nexus.seguridad.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.seguridad.be.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void listarUsuarios_debeRetornarUsuariosConvertidosADto() {
        Usuario usuario1 = crearUsuario(1, "hwongu", "clave", true);
        Usuario usuario2 = crearUsuario(2, "mlopez", "secreto", false);
        when(usuarioRepository.findAllByOrderByIdUsuarioAsc()).thenReturn(List.of(usuario1, usuario2));

        List<UsuarioDTO> resultado = usuarioService.listarUsuarios();

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getIdUsuario());
        assertEquals("hwongu", resultado.get(0).getUsername());
        assertEquals(true, resultado.get(0).getEstado());
        assertEquals(2, resultado.get(1).getIdUsuario());
        assertEquals("mlopez", resultado.get(1).getUsername());
        assertEquals(false, resultado.get(1).getEstado());
        verify(usuarioRepository).findAllByOrderByIdUsuarioAsc();
    }

    @Test
    void listarUsuarios_debeRetornarListaVaciaCuandoNoHayRegistros() {
        when(usuarioRepository.findAllByOrderByIdUsuarioAsc()).thenReturn(List.of());

        List<UsuarioDTO> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
        verify(usuarioRepository).findAllByOrderByIdUsuarioAsc();
    }

    @Test
    void buscarUsuarioPorId_debeRetornarUsuarioCuandoExiste() {
        Usuario usuario = crearUsuario(1, "hwongu", "clave", true);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        UsuarioDTO resultado = usuarioService.buscarUsuarioPorId(1);

        assertEquals(1, resultado.getIdUsuario());
        assertEquals("hwongu", resultado.getUsername());
        assertEquals(true, resultado.getEstado());
        verify(usuarioRepository).findById(1);
    }

    @Test
    void buscarUsuarioPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.buscarUsuarioPorId(99)
        );

        assertEquals("Usuario no encontrado.", exception.getMessage());
        verify(usuarioRepository).findById(99);
    }

    @Test
    void registrarUsuario_debeGuardarUsuarioConIdNuloYRetornarDto() {
        UsuarioDTO request = UsuarioDTO.builder()
                .idUsuario(88)
                .username("hwongu")
                .password("clave")
                .estado(true)
                .build();
        Usuario usuarioGuardado = crearUsuario(5, "hwongu", "clave", true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        UsuarioDTO resultado = usuarioService.registrarUsuario(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioEnviadoAGuardar = captor.getValue();

        assertNull(usuarioEnviadoAGuardar.getIdUsuario());
        assertEquals("hwongu", usuarioEnviadoAGuardar.getUsername());
        assertEquals("clave", usuarioEnviadoAGuardar.getPassword());
        assertEquals(true, usuarioEnviadoAGuardar.getEstado());
        assertEquals(5, resultado.getIdUsuario());
        assertEquals("hwongu", resultado.getUsername());
        assertEquals(true, resultado.getEstado());
    }

    @Test
    void actualizarUsuario_debeActualizarDatosCuandoExiste() {
        Usuario usuarioExistente = crearUsuario(3, "hwongu", "clave", true);
        UsuarioDTO request = UsuarioDTO.builder()
                .username("mlopez")
                .password("nueva-clave")
                .estado(false)
                .build();
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(usuarioExistente));

        usuarioService.actualizarUsuario(3, request);

        verify(usuarioRepository).findById(3);
        verify(usuarioRepository).save(usuarioExistente);
        assertEquals("mlopez", usuarioExistente.getUsername());
        assertEquals("nueva-clave", usuarioExistente.getPassword());
        assertEquals(false, usuarioExistente.getEstado());
    }

    @Test
    void actualizarUsuario_debeLanzarExcepcionCuandoNoExiste() {
        UsuarioDTO request = UsuarioDTO.builder()
                .username("mlopez")
                .password("nueva-clave")
                .estado(false)
                .build();
        when(usuarioRepository.findById(7)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.actualizarUsuario(7, request)
        );

        assertEquals("Usuario no encontrado.", exception.getMessage());
        verify(usuarioRepository).findById(7);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void eliminarUsuario_debeEliminarYHacerFlushCuandoExiste() {
        when(usuarioRepository.existsById(4)).thenReturn(true);

        usuarioService.eliminarUsuario(4);

        verify(usuarioRepository).existsById(4);
        verify(usuarioRepository).deleteById(4);
        verify(usuarioRepository).flush();
    }

    @Test
    void eliminarUsuario_debeLanzarExcepcionCuandoNoExiste() {
        when(usuarioRepository.existsById(10)).thenReturn(false);

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.eliminarUsuario(10)
        );

        assertEquals("Usuario no encontrado.", exception.getMessage());
        verify(usuarioRepository).existsById(10);
        verify(usuarioRepository, never()).deleteById(10);
        verify(usuarioRepository, never()).flush();
    }

    @Test
    void eliminarUsuario_debeTraducirErrorDeIntegridadReferencial() {
        when(usuarioRepository.existsById(4)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(usuarioRepository).flush();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioService.eliminarUsuario(4)
        );

        assertEquals(
                "No se puede eliminar el usuario porque tiene ingresos registrados a su nombre.",
                exception.getMessage()
        );
        verify(usuarioRepository).deleteById(4);
        verify(usuarioRepository).flush();
    }

    @Test
    void autenticarUsuario_debeRetornarUsuarioCuandoCredencialesSonValidas() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("hwongu")
                .password("clave")
                .build();
        Usuario usuario = crearUsuario(1, "hwongu", "clave", true);
        when(usuarioRepository.findByUsernameAndPasswordAndEstadoTrue("hwongu", "clave"))
                .thenReturn(Optional.of(usuario));

        UsuarioDTO resultado = usuarioService.autenticarUsuario(request);

        assertEquals(1, resultado.getIdUsuario());
        assertEquals("hwongu", resultado.getUsername());
        assertEquals(true, resultado.getEstado());
        verify(usuarioRepository).findByUsernameAndPasswordAndEstadoTrue("hwongu", "clave");
    }

    @Test
    void autenticarUsuario_debeLanzarExcepcionCuandoCredencialesSonInvalidas() {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("hwongu")
                .password("incorrecta")
                .build();
        when(usuarioRepository.findByUsernameAndPasswordAndEstadoTrue("hwongu", "incorrecta"))
                .thenReturn(Optional.empty());

        NoAutorizadoException exception = assertThrows(
                NoAutorizadoException.class,
                () -> usuarioService.autenticarUsuario(request)
        );

        assertEquals("Credenciales invalidas.", exception.getMessage());
        verify(usuarioRepository).findByUsernameAndPasswordAndEstadoTrue("hwongu", "incorrecta");
    }

    private Usuario crearUsuario(Integer id, String username, String password, Boolean estado) {
        return Usuario.builder()
                .idUsuario(id)
                .username(username)
                .password(password)
                .estado(estado)
                .build();
    }
}