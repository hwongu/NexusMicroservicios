package net.hwongu.nexus.seguridad.be.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwongu.nexus.seguridad.be.dto.LoginRequestDTO;
import net.hwongu.nexus.seguridad.be.dto.UsuarioDTO;
import net.hwongu.nexus.seguridad.be.exception.GlobalExceptionHandler;
import net.hwongu.nexus.seguridad.be.exception.NoAutorizadoException;
import net.hwongu.nexus.seguridad.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.seguridad.be.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void listarUsuarios_debeRetornarContratoJsonEsperado() throws Exception {
        List<UsuarioDTO> usuarios = List.of(
                UsuarioDTO.builder().idUsuario(1).username("hwongu").estado(true).build(),
                UsuarioDTO.builder().idUsuario(2).username("mlopez").estado(false).build()
        );
        when(usuarioService.listarUsuarios()).thenReturn(usuarios);

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$[0].idUsuario").value(1))
                .andExpect(jsonPath("$[0].username").value("hwongu"))
                .andExpect(jsonPath("$[0].estado").value(true))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].idUsuario").value(2))
                .andExpect(jsonPath("$[1].username").value("mlopez"))
                .andExpect(jsonPath("$[1].estado").value(false));

        verify(usuarioService).listarUsuarios();
    }

    @Test
    void buscarUsuarioPorId_debeRetornarContratoJsonEsperado() throws Exception {
        UsuarioDTO usuario = UsuarioDTO.builder()
                .idUsuario(1)
                .username("hwongu")
                .estado(true)
                .build();
        when(usuarioService.buscarUsuarioPorId(1)).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.username").value("hwongu"))
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(usuarioService).buscarUsuarioPorId(1);
    }

    @Test
    void buscarUsuarioPorId_debeRetornarError404ConFormatoEstandar() throws Exception {
        when(usuarioService.buscarUsuarioPorId(99))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado."));

        mockMvc.perform(get("/api/usuarios/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado."))
                .andExpect(jsonPath("$.path").value("/api/usuarios/99"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(usuarioService).buscarUsuarioPorId(99);
    }

    @Test
    void registrarUsuario_debeAceptarJsonValidoYRetornarContratoCreado() throws Exception {
        String request = """
                {
                  "username": "hwongu",
                  "password": "123456",
                  "estado": true
                }
                """;
        UsuarioDTO response = UsuarioDTO.builder()
                .idUsuario(1)
                .username("hwongu")
                .estado(true)
                .build();
        when(usuarioService.registrarUsuario(any(UsuarioDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.username").value("hwongu"))
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(usuarioService).registrarUsuario(any(UsuarioDTO.class));
    }

    @Test
    void registrarUsuario_debeRetornarError400CuandoBodyEsInvalido() throws Exception {
        String bodyInvalido = """
                {
                  "username": "   ",
                  "password": "",
                  "estado": null
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message.username").value("El username es obligatorio."))
                .andExpect(jsonPath("$.message.password").value("El password es obligatorio."))
                .andExpect(jsonPath("$.message.estado").value("El estado del usuario es obligatorio."))
                .andExpect(jsonPath("$.path").value("/api/usuarios"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(usuarioService, never()).registrarUsuario(any(UsuarioDTO.class));
    }

    @Test
    void actualizarUsuario_debeAceptarJsonValidoYRetornarMensajeEsperado() throws Exception {
        String request = """
                {
                  "username": "mlopez",
                  "password": "nueva-clave",
                  "estado": false
                }
                """;
        doNothing().when(usuarioService).actualizarUsuario(any(Integer.class), any(UsuarioDTO.class));

        mockMvc.perform(put("/api/usuarios/{id}", 1)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Usuario actualizado exitosamente"));

        verify(usuarioService).actualizarUsuario(any(Integer.class), any(UsuarioDTO.class));
    }

    @Test
    void actualizarUsuario_debeRetornarError404CuandoNoExiste() throws Exception {
        String request = """
                {
                  "username": "mlopez",
                  "password": "nueva-clave",
                  "estado": false
                }
                """;
        doThrow(new RecursoNoEncontradoException("Usuario no encontrado."))
                .when(usuarioService).actualizarUsuario(any(Integer.class), any(UsuarioDTO.class));

        mockMvc.perform(put("/api/usuarios/{id}", 88)
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado."))
                .andExpect(jsonPath("$.path").value("/api/usuarios/88"));

        verify(usuarioService).actualizarUsuario(any(Integer.class), any(UsuarioDTO.class));
    }

    @Test
    void eliminarUsuario_debeRetornarNoContent() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(1);

        mockMvc.perform(delete("/api/usuarios/{id}", 1))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(usuarioService).eliminarUsuario(1);
    }

    @Test
    void eliminarUsuario_debeRetornarError409CuandoHayConflictoReferencial() throws Exception {
        doThrow(new DataIntegrityViolationException("No se puede eliminar el usuario porque tiene ingresos registrados a su nombre."))
                .when(usuarioService).eliminarUsuario(1);

        mockMvc.perform(delete("/api/usuarios/{id}", 1))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("No se puede eliminar el usuario porque tiene ingresos registrados a su nombre."))
                .andExpect(jsonPath("$.path").value("/api/usuarios/1"));

        verify(usuarioService).eliminarUsuario(1);
    }

    @Test
    void login_debeAceptarJsonValidoYRetornarContratoEsperado() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("hwongu")
                .password("123456")
                .build();
        UsuarioDTO response = UsuarioDTO.builder()
                .idUsuario(1)
                .username("hwongu")
                .estado(true)
                .build();
        when(usuarioService.autenticarUsuario(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.username").value("hwongu"))
                .andExpect(jsonPath("$.estado").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(usuarioService).autenticarUsuario(any(LoginRequestDTO.class));
    }

    @Test
    void login_debeRetornarError401CuandoCredencialesSonInvalidas() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("hwongu")
                .password("incorrecta")
                .build();
        when(usuarioService.autenticarUsuario(any(LoginRequestDTO.class)))
                .thenThrow(new NoAutorizadoException("Credenciales invalidas."));

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciales invalidas."))
                .andExpect(jsonPath("$.path").value("/api/usuarios/login"));

        verify(usuarioService).autenticarUsuario(any(LoginRequestDTO.class));
    }
}
