package net.hwongu.nexus.ingreso.be.service;

import net.hwongu.nexus.ingreso.be.dto.ActualizarStockRequestDTO;
import net.hwongu.nexus.ingreso.be.dto.DetalleIngresoDTO;
import net.hwongu.nexus.ingreso.be.dto.IngresoDTO;
import net.hwongu.nexus.ingreso.be.dto.ProductoRemotoDTO;
import net.hwongu.nexus.ingreso.be.dto.RegistrarIngresoRequestDTO;
import net.hwongu.nexus.ingreso.be.dto.UsuarioRemotoDTO;
import net.hwongu.nexus.ingreso.be.entity.DetalleIngreso;
import net.hwongu.nexus.ingreso.be.entity.Ingreso;
import net.hwongu.nexus.ingreso.be.exception.BadRequestException;
import net.hwongu.nexus.ingreso.be.exception.IntegracionRemotaException;
import net.hwongu.nexus.ingreso.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.ingreso.be.repository.DetalleIngresoRepository;
import net.hwongu.nexus.ingreso.be.repository.IngresoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica el servicio de ingresos.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@ExtendWith(MockitoExtension.class)
class IngresoServiceTest {

    private static final String SEGURIDAD_BASE_URL = "http://seguridad.test";
    private static final String CATALOGO_BASE_URL = "http://catalogo.test";
    private static final LocalDateTime FECHA_FIJA = LocalDateTime.of(2026, 3, 27, 10, 30);

    @Mock
    private IngresoRepository ingresoRepository;

    @Mock
    private DetalleIngresoRepository detalleIngresoRepository;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient.Builder seguridadRestClientBuilder;

    @Mock
    private RestClient.Builder catalogoRestClientBuilder;

    @Mock
    private RestClient seguridadClient;

    @Mock
    private RestClient catalogoClient;

    @Mock
    private RestClient.RequestHeadersUriSpec seguridadGetSpec;

    @Mock
    private RestClient.RequestHeadersUriSpec catalogoGetSpec;

    @Mock
    private RestClient.RequestBodyUriSpec catalogoPutSpec;

    @Mock
    private RestClient.ResponseSpec seguridadResponseSpec;

    @Mock
    private RestClient.ResponseSpec catalogoResponseSpec;

    @Mock
    private RestClient.ResponseSpec catalogoPutResponseSpec;

    private IngresoService ingresoService;

    @BeforeEach
    void setUp() {
        ingresoService = new IngresoService(ingresoRepository, detalleIngresoRepository, restClientBuilder);
        ReflectionTestUtils.setField(ingresoService, "seguridadBaseUrl", SEGURIDAD_BASE_URL);
        ReflectionTestUtils.setField(ingresoService, "catalogoBaseUrl", CATALOGO_BASE_URL);
    }

    @Test
    void listarIngresos_debeRetornarListaEnriquecidaCuandoSeguridadResponde() {
        Ingreso ingreso = crearIngreso(10, 7, FECHA_FIJA, "RECIBIDO");
        when(ingresoRepository.findAllByOrderByIdIngresoDesc()).thenReturn(List.of(ingreso));
        prepararUsuarioOpcional(7, crearUsuarioRemoto(7, "hwongu", true));

        List<IngresoDTO> resultado = ingresoService.listarIngresos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getIdIngreso()).isEqualTo(10);
        assertThat(resultado.getFirst().getIdUsuario()).isEqualTo(7);
        assertThat(resultado.getFirst().getUsername()).isEqualTo("hwongu");
        assertThat(resultado.getFirst().getFechaIngreso()).isEqualTo(FECHA_FIJA);
        assertThat(resultado.getFirst().getEstado()).isEqualTo("RECIBIDO");
    }

    @Test
    void listarIngresos_debeLanzarIntegracionRemotaExceptionCuandoSeguridadNoResponde() {
        Ingreso ingreso = crearIngreso(10, 7, FECHA_FIJA, "RECIBIDO");
        when(ingresoRepository.findAllByOrderByIdIngresoDesc()).thenReturn(List.of(ingreso));
        prepararFalloConsultaUsuario(7);

        assertThatThrownBy(() -> ingresoService.listarIngresos())
                .isInstanceOf(IntegracionRemotaException.class)
                .hasMessage("No se pudo consultar NexusSeguridadMs. El servicio de seguridad esta caido o no disponible.");
    }

    @Test
    void buscarDetallesPorIngreso_debeRetornarDetallesEnriquecidosCuandoCatalogoResponde() {
        Integer idIngreso = 20;
        Ingreso ingreso = crearIngreso(idIngreso, 7, FECHA_FIJA, "RECIBIDO");
        DetalleIngreso detalle = crearDetalle(30, ingreso, 5, 4, 19.5);

        when(ingresoRepository.existsById(idIngreso)).thenReturn(true);
        when(detalleIngresoRepository.findAllByIngresoIdIngresoOrderByIdDetalleAsc(idIngreso))
                .thenReturn(List.of(detalle));
        prepararProductoOpcional(5, crearProductoRemoto(5, "Monitor"));

        List<DetalleIngresoDTO> resultado = ingresoService.buscarDetallesPorIngreso(idIngreso);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getIdDetalle()).isEqualTo(30);
        assertThat(resultado.getFirst().getIdIngreso()).isEqualTo(idIngreso);
        assertThat(resultado.getFirst().getIdProducto()).isEqualTo(5);
        assertThat(resultado.getFirst().getNombreProducto()).isEqualTo("Monitor");
        assertThat(resultado.getFirst().getCantidad()).isEqualTo(4);
        assertThat(resultado.getFirst().getPrecioCompra()).isEqualTo(19.5);
    }

    @Test
    void buscarDetallesPorIngreso_debeLanzarExcepcionCuandoIngresoNoExiste() {
        when(ingresoRepository.existsById(20)).thenReturn(false);

        assertThatThrownBy(() -> ingresoService.buscarDetallesPorIngreso(20))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Ingreso no encontrado.");

        verify(detalleIngresoRepository, never()).findAllByIngresoIdIngresoOrderByIdDetalleAsc(any());
    }

    @Test
    void buscarDetallesPorIngreso_debeLanzarIntegracionRemotaExceptionCuandoCatalogoNoResponde() {
        Integer idIngreso = 20;
        Ingreso ingreso = crearIngreso(idIngreso, 7, FECHA_FIJA, "RECIBIDO");
        DetalleIngreso detalle = crearDetalle(30, ingreso, 5, 4, 19.5);

        when(ingresoRepository.existsById(idIngreso)).thenReturn(true);
        when(detalleIngresoRepository.findAllByIngresoIdIngresoOrderByIdDetalleAsc(idIngreso))
                .thenReturn(List.of(detalle));
        prepararFalloConsultaProducto(5);

        assertThatThrownBy(() -> ingresoService.buscarDetallesPorIngreso(idIngreso))
                .isInstanceOf(IntegracionRemotaException.class)
                .hasMessage("No se pudo consultar NexusCatalogoMs. El servicio de catalogo esta caido o no disponible.");
    }

    @Test
    void registrarIngresoCompleto_debeGuardarYActualizarStockCuandoTodoEsValido() {
        RegistrarIngresoRequestDTO request = crearSolicitudRegistro(7, 5, 4, 19.5);
        when(ingresoRepository.save(any(Ingreso.class))).thenAnswer(invocation -> {
            Ingreso ingreso = invocation.getArgument(0);
            if (ingreso.getIdIngreso() == null) {
                ingreso.setIdIngreso(100);
            }
            return ingreso;
        });
        when(detalleIngresoRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        prepararUsuarioValidado(7, crearUsuarioRemoto(7, "hwongu", true));
        prepararProductoValidado(5, crearProductoRemoto(5, "Monitor"));
        prepararActualizacionStockExitosa(5);

        IngresoDTO resultado = ingresoService.registrarIngresoCompleto(request);

        assertThat(resultado.getIdIngreso()).isEqualTo(100);
        assertThat(resultado.getIdUsuario()).isEqualTo(7);
        assertThat(resultado.getUsername()).isEqualTo("hwongu");
        assertThat(resultado.getFechaIngreso()).isEqualTo(FECHA_FIJA);
        assertThat(resultado.getEstado()).isEqualTo("RECIBIDO");

        ArgumentCaptor<Ingreso> ingresoCaptor = ArgumentCaptor.forClass(Ingreso.class);
        verify(ingresoRepository, times(2)).save(ingresoCaptor.capture());
        assertThat(ingresoCaptor.getAllValues().getLast().getEstado()).isEqualTo("RECIBIDO");

        ArgumentCaptor<ActualizarStockRequestDTO> stockCaptor = ArgumentCaptor.forClass(ActualizarStockRequestDTO.class);
        verify(catalogoPutSpec).body(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getCantidad()).isEqualTo(4);
        assertThat(stockCaptor.getValue().getOperacion()).isEqualTo("SUMAR");
    }

    @Test
    void registrarIngresoCompleto_debeRechazarCuandoElUsuarioEstaInactivo() {
        RegistrarIngresoRequestDTO request = crearSolicitudRegistro(7, 5, 4, 19.5);
        prepararUsuarioValidado(7, crearUsuarioRemoto(7, "hwongu", false));

        assertThatThrownBy(() -> ingresoService.registrarIngresoCompleto(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El usuario indicado no existe o esta inactivo.");

        verify(ingresoRepository, never()).save(any(Ingreso.class));
        verify(detalleIngresoRepository, never()).saveAll(anyList());
    }

    @Test
    void registrarIngresoCompleto_debeRechazarCuandoUnProductoNoExiste() {
        RegistrarIngresoRequestDTO request = crearSolicitudRegistro(7, 5, 4, 19.5);
        prepararUsuarioValidado(7, crearUsuarioRemoto(7, "hwongu", true));
        prepararProductoValidadoConBadRequest(5, "El producto indicado no existe.");

        assertThatThrownBy(() -> ingresoService.registrarIngresoCompleto(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El producto indicado no existe.");

        verify(ingresoRepository, never()).save(any(Ingreso.class));
        verify(detalleIngresoRepository, never()).saveAll(anyList());
    }

    @Test
    void registrarIngresoCompleto_debeMarcarErrorIntegracionCuandoFallaActualizacionRemotaDeStock() {
        RegistrarIngresoRequestDTO request = crearSolicitudRegistro(7, 5, 4, 19.5);
        when(ingresoRepository.save(any(Ingreso.class))).thenAnswer(invocation -> {
            Ingreso ingreso = invocation.getArgument(0);
            if (ingreso.getIdIngreso() == null) {
                ingreso.setIdIngreso(100);
            }
            return ingreso;
        });
        when(detalleIngresoRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        prepararUsuarioValidado(7, crearUsuarioRemoto(7, "hwongu", true));
        prepararProductoValidado(5, crearProductoRemoto(5, "Monitor"));
        prepararFalloActualizacionStock(5);

        assertThatThrownBy(() -> ingresoService.registrarIngresoCompleto(request))
                .isInstanceOf(IntegracionRemotaException.class)
                .hasMessage("El ingreso fue registrado localmente, pero no se pudo actualizar el stock en catalogo. El ingreso quedo en estado ERROR_INTEGRACION.");

        ArgumentCaptor<Ingreso> ingresoCaptor = ArgumentCaptor.forClass(Ingreso.class);
        verify(ingresoRepository, times(2)).save(ingresoCaptor.capture());
        assertThat(ingresoCaptor.getAllValues().getLast().getEstado()).isEqualTo("ERROR_INTEGRACION");
    }

    @Test
    void anularIngreso_debeRevertirStockYMarcarIngresoComoAnulado() {
        Ingreso ingreso = crearIngreso(50, 7, FECHA_FIJA, "RECIBIDO");
        DetalleIngreso detalle = crearDetalle(60, ingreso, 5, 4, 19.5);
        when(ingresoRepository.findById(50)).thenReturn(Optional.of(ingreso));
        when(detalleIngresoRepository.findAllByIngresoIdIngresoOrderByIdDetalleAsc(50)).thenReturn(List.of(detalle));
        prepararActualizacionStockExitosa(5);

        ingresoService.anularIngreso(50);

        ArgumentCaptor<Ingreso> ingresoCaptor = ArgumentCaptor.forClass(Ingreso.class);
        verify(ingresoRepository).save(ingresoCaptor.capture());
        assertThat(ingresoCaptor.getValue().getEstado()).isEqualTo("ANULADO");

        ArgumentCaptor<ActualizarStockRequestDTO> stockCaptor = ArgumentCaptor.forClass(ActualizarStockRequestDTO.class);
        verify(catalogoPutSpec).body(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getOperacion()).isEqualTo("RESTAR");
        assertThat(stockCaptor.getValue().getCantidad()).isEqualTo(4);
    }

    @Test
    void anularIngreso_debeLanzarExcepcionCuandoIngresoNoExiste() {
        when(ingresoRepository.findById(50)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingresoService.anularIngreso(50))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Ingreso no encontrado.");
    }

    @Test
    void anularIngreso_debeRechazarCuandoYaEstaAnulado() {
        Ingreso ingreso = crearIngreso(50, 7, FECHA_FIJA, "ANULADO");
        when(ingresoRepository.findById(50)).thenReturn(Optional.of(ingreso));

        assertThatThrownBy(() -> ingresoService.anularIngreso(50))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El ingreso ya se encuentra anulado.");

        verify(detalleIngresoRepository, never()).findAllByIngresoIdIngresoOrderByIdDetalleAsc(any());
    }

    @Test
    void anularIngreso_debeRechazarCuandoElEstadoNoPermiteAnular() {
        Ingreso ingreso = crearIngreso(50, 7, FECHA_FIJA, "PENDIENTE");
        when(ingresoRepository.findById(50)).thenReturn(Optional.of(ingreso));

        assertThatThrownBy(() -> ingresoService.anularIngreso(50))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Solo se puede anular un ingreso en estado RECIBIDO.");
    }

    @Test
    void anularIngreso_debeMarcarErrorIntegracionCuandoFallaLaReversionDeStock() {
        Ingreso ingreso = crearIngreso(50, 7, FECHA_FIJA, "RECIBIDO");
        DetalleIngreso detalle = crearDetalle(60, ingreso, 5, 4, 19.5);
        when(ingresoRepository.findById(50)).thenReturn(Optional.of(ingreso));
        when(detalleIngresoRepository.findAllByIngresoIdIngresoOrderByIdDetalleAsc(50)).thenReturn(List.of(detalle));
        prepararFalloActualizacionStock(5);

        assertThatThrownBy(() -> ingresoService.anularIngreso(50))
                .isInstanceOf(IntegracionRemotaException.class)
                .hasMessage("No se pudo revertir el stock en catalogo. El ingreso quedo en estado ERROR_INTEGRACION.");

        ArgumentCaptor<Ingreso> ingresoCaptor = ArgumentCaptor.forClass(Ingreso.class);
        verify(ingresoRepository).save(ingresoCaptor.capture());
        assertThat(ingresoCaptor.getValue().getEstado()).isEqualTo("ERROR_INTEGRACION");
    }

    @Test
    void actualizarEstadoIngreso_debeNormalizarYGuardarElNuevoEstado() {
        Ingreso ingreso = crearIngreso(70, 9, FECHA_FIJA, "PENDIENTE");
        when(ingresoRepository.findById(70)).thenReturn(Optional.of(ingreso));

        ingresoService.actualizarEstadoIngreso(70, " recibido ");

        ArgumentCaptor<Ingreso> ingresoCaptor = ArgumentCaptor.forClass(Ingreso.class);
        verify(ingresoRepository).save(ingresoCaptor.capture());
        assertThat(ingresoCaptor.getValue().getEstado()).isEqualTo("RECIBIDO");
    }

    @Test
    void actualizarEstadoIngreso_debeLanzarExcepcionCuandoIngresoNoExiste() {
        when(ingresoRepository.findById(70)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingresoService.actualizarEstadoIngreso(70, "RECIBIDO"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Ingreso no encontrado.");
    }

    private void prepararUsuarioOpcional(Integer idUsuario, UsuarioRemotoDTO usuarioRemotoDTO) {
        prepararBaseSeguridadOpcional();
        when(seguridadGetSpec.uri("/api/usuarios/{id}", idUsuario)).thenReturn(seguridadGetSpec);
        when(seguridadResponseSpec.body(UsuarioRemotoDTO.class)).thenReturn(usuarioRemotoDTO);
    }

    private void prepararUsuarioValidado(Integer idUsuario, UsuarioRemotoDTO usuarioRemotoDTO) {
        prepararBaseSeguridadValidacion();
        when(seguridadGetSpec.uri("/api/usuarios/{id}", idUsuario)).thenReturn(seguridadGetSpec);
        when(seguridadResponseSpec.body(UsuarioRemotoDTO.class)).thenReturn(usuarioRemotoDTO);
    }

    private void prepararFalloConsultaUsuario(Integer idUsuario) {
        prepararBaseSeguridadOpcional();
        when(seguridadGetSpec.uri("/api/usuarios/{id}", idUsuario)).thenReturn(seguridadGetSpec);
        when(seguridadGetSpec.retrieve()).thenThrow(new RestClientException("seguridad caida") { });
    }

    private void prepararProductoOpcional(Integer idProducto, ProductoRemotoDTO productoRemotoDTO) {
        prepararBaseCatalogoConsultaOpcional();
        when(catalogoGetSpec.uri("/api/productos/{id}", idProducto)).thenReturn(catalogoGetSpec);
        when(catalogoResponseSpec.body(ProductoRemotoDTO.class)).thenReturn(productoRemotoDTO);
    }

    private void prepararProductoValidado(Integer idProducto, ProductoRemotoDTO productoRemotoDTO) {
        prepararBaseCatalogoConsultaValidacion();
        when(catalogoGetSpec.uri("/api/productos/{id}", idProducto)).thenReturn(catalogoGetSpec);
        when(catalogoResponseSpec.body(ProductoRemotoDTO.class)).thenReturn(productoRemotoDTO);
    }

    private void prepararProductoValidadoConBadRequest(Integer idProducto, String mensaje) {
        prepararBaseCatalogoConsultaValidacion();
        when(catalogoGetSpec.uri("/api/productos/{id}", idProducto)).thenReturn(catalogoGetSpec);
        when(catalogoResponseSpec.body(ProductoRemotoDTO.class)).thenThrow(new BadRequestException(mensaje));
    }

    private void prepararFalloConsultaProducto(Integer idProducto) {
        prepararBaseCatalogoConsultaOpcional();
        when(catalogoGetSpec.uri("/api/productos/{id}", idProducto)).thenReturn(catalogoGetSpec);
        when(catalogoGetSpec.retrieve()).thenThrow(new RestClientException("catalogo caido") { });
    }

    private void prepararActualizacionStockExitosa(Integer idProducto) {
        prepararBaseCatalogoStock();
        when(catalogoPutSpec.uri("/api/productos/{id}/stock", idProducto)).thenReturn(catalogoPutSpec);
        when(catalogoPutSpec.retrieve()).thenReturn(catalogoPutResponseSpec);
        when(catalogoPutResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    private void prepararFalloActualizacionStock(Integer idProducto) {
        prepararBaseCatalogoStock();
        when(catalogoPutSpec.uri("/api/productos/{id}/stock", idProducto)).thenReturn(catalogoPutSpec);
        when(catalogoPutSpec.retrieve()).thenThrow(new RestClientException("catalogo caido") { });
    }

    private void prepararBaseSeguridadOpcional() {
        when(restClientBuilder.baseUrl(SEGURIDAD_BASE_URL)).thenReturn(seguridadRestClientBuilder);
        when(seguridadRestClientBuilder.build()).thenReturn(seguridadClient);
        when(seguridadClient.get()).thenReturn(seguridadGetSpec);
        when(seguridadGetSpec.retrieve()).thenReturn(seguridadResponseSpec);
    }

    private void prepararBaseSeguridadValidacion() {
        prepararBaseSeguridadOpcional();
        when(seguridadResponseSpec.onStatus(
                org.mockito.Mockito.<Predicate<HttpStatusCode>>any(),
                org.mockito.Mockito.<RestClient.ResponseSpec.ErrorHandler>any())
        ).thenReturn(seguridadResponseSpec);
    }

    private void prepararBaseCatalogoConsultaOpcional() {
        when(restClientBuilder.baseUrl(CATALOGO_BASE_URL)).thenReturn(catalogoRestClientBuilder);
        when(catalogoRestClientBuilder.build()).thenReturn(catalogoClient);
        when(catalogoClient.get()).thenReturn(catalogoGetSpec);
        when(catalogoGetSpec.retrieve()).thenReturn(catalogoResponseSpec);
    }

    private void prepararBaseCatalogoConsultaValidacion() {
        prepararBaseCatalogoConsultaOpcional();
        when(catalogoResponseSpec.onStatus(
                org.mockito.Mockito.<Predicate<HttpStatusCode>>any(),
                org.mockito.Mockito.<RestClient.ResponseSpec.ErrorHandler>any())
        ).thenReturn(catalogoResponseSpec);
    }

    private void prepararBaseCatalogoStock() {
        when(restClientBuilder.baseUrl(CATALOGO_BASE_URL)).thenReturn(catalogoRestClientBuilder);
        when(catalogoRestClientBuilder.build()).thenReturn(catalogoClient);
        when(catalogoClient.put()).thenReturn(catalogoPutSpec);
        doReturn(catalogoPutSpec).when(catalogoPutSpec).body(any(Object.class));
    }

    private RegistrarIngresoRequestDTO crearSolicitudRegistro(Integer idUsuario, Integer idProducto, Integer cantidad, Double precioCompra) {
        IngresoDTO ingreso = IngresoDTO.builder()
                .idUsuario(idUsuario)
                .fechaIngreso(FECHA_FIJA)
                .build();

        DetalleIngresoDTO detalle = DetalleIngresoDTO.builder()
                .idProducto(idProducto)
                .cantidad(cantidad)
                .precioCompra(precioCompra)
                .build();

        return RegistrarIngresoRequestDTO.builder()
                .ingreso(ingreso)
                .detalles(List.of(detalle))
                .build();
    }

    private Ingreso crearIngreso(Integer idIngreso, Integer idUsuario, LocalDateTime fechaIngreso, String estado) {
        return Ingreso.builder()
                .idIngreso(idIngreso)
                .idUsuario(idUsuario)
                .fechaIngreso(fechaIngreso)
                .estado(estado)
                .build();
    }

    private DetalleIngreso crearDetalle(Integer idDetalle, Ingreso ingreso, Integer idProducto, Integer cantidad, Double precioCompra) {
        return DetalleIngreso.builder()
                .idDetalle(idDetalle)
                .ingreso(ingreso)
                .idProducto(idProducto)
                .cantidad(cantidad)
                .precioCompra(precioCompra)
                .build();
    }

    private UsuarioRemotoDTO crearUsuarioRemoto(Integer idUsuario, String username, Boolean estado) {
        return UsuarioRemotoDTO.builder()
                .idUsuario(idUsuario)
                .username(username)
                .estado(estado)
                .build();
    }

    private ProductoRemotoDTO crearProductoRemoto(Integer idProducto, String nombre) {
        return ProductoRemotoDTO.builder()
                .idProducto(idProducto)
                .nombre(nombre)
                .build();
    }
}
