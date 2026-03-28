package net.hwongu.nexus.ingreso.be.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Capa de servicio para operaciones de ingresos.
 *
 * <p>Ademas de coordinar la persistencia local, esta capa valida usuarios y
 * productos mediante llamadas HTTP simples hacia los otros microservicios.
 * Esto permite explicar de forma didactica la diferencia entre persistencia
 * propia e integracion remota.</p>
 *
 * @author Henry Wong
 */
@Service
@RequiredArgsConstructor
public class IngresoService {

    private static final String OPERACION_SUMAR = "SUMAR";
    private static final String OPERACION_RESTAR = "RESTAR";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_RECIBIDO = "RECIBIDO";
    private static final String ESTADO_ANULADO = "ANULADO";
    private static final String ESTADO_ERROR_INTEGRACION = "ERROR_INTEGRACION";
    private static final String MENSAJE_SEGURIDAD_NO_DISPONIBLE =
            "No se pudo consultar NexusSeguridadMs. El servicio de seguridad esta caido o no disponible.";
    private static final String MENSAJE_CATALOGO_NO_DISPONIBLE =
            "No se pudo consultar NexusCatalogoMs. El servicio de catalogo esta caido o no disponible.";

    private final IngresoRepository ingresoRepository;
    private final DetalleIngresoRepository detalleIngresoRepository;
    private final RestClient.Builder restClientBuilder;

    @Value("${app.seguridad.base-url}")
    private String seguridadBaseUrl;

    @Value("${app.catalogo.base-url}")
    private String catalogoBaseUrl;

    /**
     * Lista todos los ingresos registrados.
     *
     * @return ingresos convertidos a DTO.
     */
    @Transactional(readOnly = true)
    public List<IngresoDTO> listarIngresos() {
        return ingresoRepository.findAllByOrderByIdIngresoDesc()
                .stream()
                .map(this::convertirIngresoADTO)
                .toList();
    }

    /**
     * Obtiene todos los detalles de un ingreso especifico.
     *
     * @param idIngreso identificador del ingreso.
     * @return detalles enriquecidos con nombre de producto si catalogo responde.
     */
    @Transactional(readOnly = true)
    public List<DetalleIngresoDTO> buscarDetallesPorIngreso(Integer idIngreso) {
        if (!ingresoRepository.existsById(idIngreso)) {
            throw new RecursoNoEncontradoException("Ingreso no encontrado.");
        }

        return detalleIngresoRepository.findAllByIngresoIdIngresoOrderByIdDetalleAsc(idIngreso)
                .stream()
                .map(this::convertirDetalleADTO)
                .toList();
    }

    /**
     * Registra un ingreso completo: valida usuario y productos, persiste datos
     * locales y luego solicita la actualizacion de stock al microservicio de
     * catalogo.
     *
     * <p>Si el ajuste remoto de stock falla, no se intenta una transaccion
     * distribuida. En su lugar, el ingreso queda marcado como
     * {@code ERROR_INTEGRACION} para dejar el problema visible y recuperable.</p>
     *
     * @param requestDTO datos de cabecera y detalle.
     * @return ingreso creado si toda la integracion se completa.
     */
    @Transactional(noRollbackFor = IntegracionRemotaException.class)
    public IngresoDTO registrarIngresoCompleto(RegistrarIngresoRequestDTO requestDTO) {
        IngresoDTO ingresoDTO = requestDTO.getIngreso();
        List<DetalleIngresoDTO> detalleDTOs = requestDTO.getDetalles();

        UsuarioRemotoDTO usuario = validarUsuarioActivo(ingresoDTO.getIdUsuario());
        detalleDTOs.forEach(detalleDTO -> validarProductoExistente(detalleDTO.getIdProducto()));

        Ingreso ingreso = Ingreso.builder()
                .idUsuario(usuario.getIdUsuario())
                .fechaIngreso(ingresoDTO.getFechaIngreso() != null ? ingresoDTO.getFechaIngreso() : LocalDateTime.now())
                .estado(ESTADO_PENDIENTE)
                .build();

        Ingreso ingresoGuardado = ingresoRepository.save(ingreso);

        List<DetalleIngreso> detalles = detalleDTOs.stream()
                .map(detalleDTO -> convertirDetalleAEntidad(detalleDTO, ingresoGuardado))
                .toList();

        detalleIngresoRepository.saveAll(detalles);

        try {
            actualizarStockRemoto(detalles, OPERACION_SUMAR);
            ingresoGuardado.setEstado(ESTADO_RECIBIDO);
            ingresoRepository.save(ingresoGuardado);
        } catch (RestClientException exception) {
            ingresoGuardado.setEstado(ESTADO_ERROR_INTEGRACION);
            ingresoRepository.save(ingresoGuardado);
            throw new IntegracionRemotaException(
                    "El ingreso fue registrado localmente, pero no se pudo actualizar el stock en catalogo. "
                            + "El ingreso quedo en estado ERROR_INTEGRACION."
            );
        }

        return construirIngresoDTO(ingresoGuardado, usuario != null ? usuario.getUsername() : null);
    }

    /**
     * Anula un ingreso y solicita a catalogo revertir el stock previamente
     * incrementado.
     *
     * @param idIngreso identificador del ingreso.
     */
    @Transactional(noRollbackFor = IntegracionRemotaException.class)
    public void anularIngreso(Integer idIngreso) {
        Ingreso ingreso = ingresoRepository.findById(idIngreso)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ingreso no encontrado."));

        if (ESTADO_ANULADO.equalsIgnoreCase(ingreso.getEstado())) {
            throw new BadRequestException("El ingreso ya se encuentra anulado.");
        }

        if (!ESTADO_RECIBIDO.equalsIgnoreCase(ingreso.getEstado())) {
            throw new BadRequestException("Solo se puede anular un ingreso en estado RECIBIDO.");
        }

        List<DetalleIngreso> detalles = detalleIngresoRepository.findAllByIngresoIdIngresoOrderByIdDetalleAsc(idIngreso);

        try {
            actualizarStockRemoto(detalles, OPERACION_RESTAR);
            ingreso.setEstado(ESTADO_ANULADO);
            ingresoRepository.save(ingreso);
        } catch (RestClientException exception) {
            ingreso.setEstado(ESTADO_ERROR_INTEGRACION);
            ingresoRepository.save(ingreso);
            throw new IntegracionRemotaException(
                    "No se pudo revertir el stock en catalogo. El ingreso quedo en estado ERROR_INTEGRACION."
            );
        }
    }

    /**
     * Actualiza manualmente el estado de un ingreso.
     *
     * @param idIngreso identificador del ingreso.
     * @param nuevoEstado nuevo estado solicitado.
     */
    @Transactional
    public void actualizarEstadoIngreso(Integer idIngreso, String nuevoEstado) {
        Ingreso ingreso = ingresoRepository.findById(idIngreso)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ingreso no encontrado."));

        ingreso.setEstado(nuevoEstado.trim().toUpperCase());
        ingresoRepository.save(ingreso);
    }

    /**
     * Convierte una entidad local a DTO enriquecido con datos del usuario
     * obtenidos por HTTP si el microservicio de seguridad esta disponible.
     *
     * @param ingreso entidad local persistida.
     * @return DTO para respuesta.
     */
    private IngresoDTO convertirIngresoADTO(Ingreso ingreso) {
        UsuarioRemotoDTO usuario = buscarUsuarioOpcional(ingreso.getIdUsuario());
        return construirIngresoDTO(ingreso, usuario.getUsername());
    }

    /**
     * Convierte una entidad local de detalle a DTO enriquecido con el nombre
     * del producto si catalogo esta disponible.
     *
     * @param detalle detalle persistido localmente.
     * @return DTO para respuesta.
     */
    private DetalleIngresoDTO convertirDetalleADTO(DetalleIngreso detalle) {
        ProductoRemotoDTO producto = buscarProductoOpcional(detalle.getIdProducto());

        return DetalleIngresoDTO.builder()
                .idDetalle(detalle.getIdDetalle())
                .idIngreso(detalle.getIngreso().getIdIngreso())
                .idProducto(detalle.getIdProducto())
                .nombreProducto(producto.getNombre())
                .cantidad(detalle.getCantidad())
                .precioCompra(detalle.getPrecioCompra())
                .build();
    }

    /**
     * Construye un DTO de ingreso usando un nombre de usuario ya resuelto.
     *
     * @param ingreso entidad local.
     * @param username nombre de usuario si se pudo recuperar.
     * @return DTO final.
     */
    private IngresoDTO construirIngresoDTO(Ingreso ingreso, String username) {
        return IngresoDTO.builder()
                .idIngreso(ingreso.getIdIngreso())
                .idUsuario(ingreso.getIdUsuario())
                .username(username)
                .fechaIngreso(ingreso.getFechaIngreso())
                .estado(ingreso.getEstado())
                .build();
    }

    /**
     * Convierte un detalle DTO a entidad local.
     *
     * @param detalleDTO datos recibidos desde la API.
     * @param ingreso ingreso padre ya persistido.
     * @return detalle listo para persistir.
     */
    private DetalleIngreso convertirDetalleAEntidad(DetalleIngresoDTO detalleDTO, Ingreso ingreso) {
        return DetalleIngreso.builder()
                .ingreso(ingreso)
                .idProducto(detalleDTO.getIdProducto())
                .cantidad(detalleDTO.getCantidad())
                .precioCompra(detalleDTO.getPrecioCompra())
                .build();
    }

    /**
     * Valida que el usuario exista y este activo consultando seguridad.
     *
     * @param idUsuario identificador a validar.
     * @return usuario remoto validado.
     */
    private UsuarioRemotoDTO validarUsuarioActivo(Integer idUsuario) {
        UsuarioRemotoDTO usuario;

        try {
            usuario = obtenerClienteSeguridad()
                    .get()
                    .uri("/api/usuarios/{id}", idUsuario)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new BadRequestException("El usuario indicado no existe.");
                    })
                    .body(UsuarioRemotoDTO.class);
        } catch (BadRequestException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new IntegracionRemotaException(MENSAJE_SEGURIDAD_NO_DISPONIBLE);
        }

        if (usuario == null || usuario.getEstado() == null || !usuario.getEstado()) {
            throw new BadRequestException("El usuario indicado no existe o esta inactivo.");
        }

        return usuario;
    }

    /**
     * Valida que el producto exista consultando catalogo.
     *
     * @param idProducto identificador del producto.
     * @return producto remoto validado.
     */
    private ProductoRemotoDTO validarProductoExistente(Integer idProducto) {
        ProductoRemotoDTO producto;

        try {
            producto = obtenerClienteCatalogo()
                    .get()
                    .uri("/api/productos/{id}", idProducto)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new BadRequestException("El producto indicado no existe.");
                    })
                    .body(ProductoRemotoDTO.class);
        } catch (BadRequestException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new IntegracionRemotaException(MENSAJE_CATALOGO_NO_DISPONIBLE);
        }

        if (producto == null) {
            throw new BadRequestException("El producto indicado no existe.");
        }

        return producto;
    }

    /**
     * Busca un usuario remoto requerido para construir la respuesta.
     *
     * @param idUsuario identificador del usuario.
     * @return usuario remoto recuperado desde seguridad.
     */
    private UsuarioRemotoDTO buscarUsuarioOpcional(Integer idUsuario) {
        UsuarioRemotoDTO usuario;

        try {
            usuario = obtenerClienteSeguridad()
                    .get()
                    .uri("/api/usuarios/{id}", idUsuario)
                    .retrieve()
                    .body(UsuarioRemotoDTO.class);
        } catch (RestClientException exception) {
            throw new IntegracionRemotaException(MENSAJE_SEGURIDAD_NO_DISPONIBLE);
        }

        if (usuario == null || usuario.getUsername() == null) {
            throw new IntegracionRemotaException(MENSAJE_SEGURIDAD_NO_DISPONIBLE);
        }

        return usuario;
    }

    /**
     * Busca un producto remoto requerido para construir la respuesta.
     *
     * @param idProducto identificador del producto.
     * @return producto remoto recuperado desde catalogo.
     */
    private ProductoRemotoDTO buscarProductoOpcional(Integer idProducto) {
        ProductoRemotoDTO producto;

        try {
            producto = obtenerClienteCatalogo()
                    .get()
                    .uri("/api/productos/{id}", idProducto)
                    .retrieve()
                    .body(ProductoRemotoDTO.class);
        } catch (RestClientException exception) {
            throw new IntegracionRemotaException(MENSAJE_CATALOGO_NO_DISPONIBLE);
        }

        if (producto == null || producto.getNombre() == null) {
            throw new IntegracionRemotaException(MENSAJE_CATALOGO_NO_DISPONIBLE);
        }

        return producto;
    }

    /**
     * Solicita a catalogo ajustar stock por cada detalle persistido localmente.
     *
     * @param detalles detalles del ingreso.
     * @param operacion tipo de ajuste solicitado.
     */
    private void actualizarStockRemoto(List<DetalleIngreso> detalles, String operacion) {
        RestClient catalogoClient = obtenerClienteCatalogo();

        for (DetalleIngreso detalle : detalles) {
            ActualizarStockRequestDTO requestDTO = ActualizarStockRequestDTO.builder()
                    .cantidad(detalle.getCantidad())
                    .operacion(operacion)
                    .build();

            catalogoClient.put()
                    .uri("/api/productos/{id}/stock", detalle.getIdProducto())
                    .body(requestDTO)
                    .retrieve()
                    .toBodilessEntity();
        }
    }

    /**
     * Construye un cliente HTTP simple hacia seguridad.
     *
     * @return cliente configurado con URL base.
     */
    private RestClient obtenerClienteSeguridad() {
        return restClientBuilder.baseUrl(seguridadBaseUrl).build();
    }

    /**
     * Construye un cliente HTTP simple hacia catalogo.
     *
     * @return cliente configurado con URL base.
     */
    private RestClient obtenerClienteCatalogo() {
        return restClientBuilder.baseUrl(catalogoBaseUrl).build();
    }
}
