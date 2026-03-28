package net.hwongu.nexus.catalogo.be.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwongu.nexus.catalogo.be.dto.ActualizarStockRequestDTO;
import net.hwongu.nexus.catalogo.be.dto.ProductoDTO;
import net.hwongu.nexus.catalogo.be.exception.BadRequestException;
import net.hwongu.nexus.catalogo.be.exception.GlobalExceptionHandler;
import net.hwongu.nexus.catalogo.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.catalogo.be.service.ProductoService;
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
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(productoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void listarProductos_debeRetornarContratoJsonEsperado() throws Exception {
        List<ProductoDTO> productos = List.of(
                ProductoDTO.builder()
                        .idProducto(1)
                        .idCategoria(10)
                        .nombreCategoria("Laptops")
                        .nombre("ThinkPad T14")
                        .precio(1200.5)
                        .stock(50)
                        .build()
        );
        when(productoService.listarProductos()).thenReturn(productos);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$[0].idProducto").value(1))
                .andExpect(jsonPath("$[0].idCategoria").value(10))
                .andExpect(jsonPath("$[0].nombreCategoria").value("Laptops"))
                .andExpect(jsonPath("$[0].nombre").value("ThinkPad T14"))
                .andExpect(jsonPath("$[0].precio").value(1200.5))
                .andExpect(jsonPath("$[0].stock").value(50));

        verify(productoService).listarProductos();
    }

    @Test
    void buscarProductoPorId_debeRetornarContratoJsonEsperado() throws Exception {
        ProductoDTO producto = ProductoDTO.builder()
                .idProducto(1)
                .idCategoria(10)
                .nombreCategoria("Laptops")
                .nombre("ThinkPad T14")
                .precio(1200.5)
                .stock(50)
                .build();
        when(productoService.buscarProductoPorId(1)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.idCategoria").value(10))
                .andExpect(jsonPath("$.nombreCategoria").value("Laptops"))
                .andExpect(jsonPath("$.nombre").value("ThinkPad T14"))
                .andExpect(jsonPath("$.precio").value(1200.5))
                .andExpect(jsonPath("$.stock").value(50));

        verify(productoService).buscarProductoPorId(1);
    }

    @Test
    void buscarProductoPorId_debeRetornarError404ConFormatoEstandar() throws Exception {
        when(productoService.buscarProductoPorId(77))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado."));

        mockMvc.perform(get("/api/productos/{id}", 77))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Producto no encontrado."))
                .andExpect(jsonPath("$.path").value("/api/productos/77"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productoService).buscarProductoPorId(77);
    }

    @Test
    void registrarProducto_debeAceptarJsonValidoYRetornarContratoCreado() throws Exception {
        ProductoDTO request = ProductoDTO.builder()
                .idCategoria(10)
                .nombre("ThinkPad T14")
                .precio(1200.5)
                .stock(50)
                .build();
        ProductoDTO response = ProductoDTO.builder()
                .idProducto(1)
                .idCategoria(10)
                .nombreCategoria("Laptops")
                .nombre("ThinkPad T14")
                .precio(1200.5)
                .stock(50)
                .build();
        when(productoService.registrarProducto(any(ProductoDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/productos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.idCategoria").value(10))
                .andExpect(jsonPath("$.nombreCategoria").value("Laptops"))
                .andExpect(jsonPath("$.nombre").value("ThinkPad T14"))
                .andExpect(jsonPath("$.precio").value(1200.5))
                .andExpect(jsonPath("$.stock").value(50));

        verify(productoService).registrarProducto(any(ProductoDTO.class));
    }

    @Test
    void registrarProducto_debeRetornarError400CuandoBodyEsInvalido() throws Exception {
        String bodyInvalido = """
                {
                  "idCategoria": 0,
                  "nombre": "",
                  "precio": 0,
                  "stock": -1
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message.idCategoria").value("El id de la categoria debe ser mayor a cero."))
                .andExpect(jsonPath("$.message.nombre").value("El nombre del producto es obligatorio."))
                .andExpect(jsonPath("$.message.precio").value("El precio debe ser mayor a cero."))
                .andExpect(jsonPath("$.message.stock").value("El stock no puede ser negativo."))
                .andExpect(jsonPath("$.path").value("/api/productos"));

        verify(productoService, never()).registrarProducto(any(ProductoDTO.class));
    }

    @Test
    void actualizarProducto_debeAceptarJsonValidoYRetornarMensajeEsperado() throws Exception {
        ProductoDTO request = ProductoDTO.builder()
                .idCategoria(10)
                .nombre("ThinkPad X1")
                .precio(1800.0)
                .stock(15)
                .build();
        doNothing().when(productoService).actualizarProducto(any(Integer.class), any(ProductoDTO.class));

        mockMvc.perform(put("/api/productos/{id}", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Producto actualizado exitosamente"));

        verify(productoService).actualizarProducto(any(Integer.class), any(ProductoDTO.class));
    }

    @Test
    void actualizarProducto_debeRetornarError404CuandoNoExiste() throws Exception {
        ProductoDTO request = ProductoDTO.builder()
                .idCategoria(10)
                .nombre("ThinkPad X1")
                .precio(1800.0)
                .stock(15)
                .build();
        doThrow(new RecursoNoEncontradoException("Producto no encontrado."))
                .when(productoService).actualizarProducto(any(Integer.class), any(ProductoDTO.class));

        mockMvc.perform(put("/api/productos/{id}", 99)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Producto no encontrado."))
                .andExpect(jsonPath("$.path").value("/api/productos/99"));

        verify(productoService).actualizarProducto(any(Integer.class), any(ProductoDTO.class));
    }

    @Test
    void actualizarStockProducto_debeAceptarJsonValidoYRetornarMensajeEsperado() throws Exception {
        ActualizarStockRequestDTO request = ActualizarStockRequestDTO.builder()
                .cantidad(10)
                .operacion("SUMAR")
                .build();
        doNothing().when(productoService).actualizarStockProducto(any(Integer.class), any(ActualizarStockRequestDTO.class));

        mockMvc.perform(put("/api/productos/{id}/stock", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Stock del producto actualizado exitosamente"));

        verify(productoService).actualizarStockProducto(any(Integer.class), any(ActualizarStockRequestDTO.class));
    }

    @Test
    void actualizarStockProducto_debeRetornarError400CuandoOperacionEsInvalida() throws Exception {
        ActualizarStockRequestDTO request = ActualizarStockRequestDTO.builder()
                .cantidad(10)
                .operacion("DIVIDIR")
                .build();
        doThrow(new BadRequestException("La operacion debe ser SUMAR o RESTAR."))
                .when(productoService).actualizarStockProducto(any(Integer.class), any(ActualizarStockRequestDTO.class));

        mockMvc.perform(put("/api/productos/{id}/stock", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("La operacion debe ser SUMAR o RESTAR."))
                .andExpect(jsonPath("$.path").value("/api/productos/1/stock"));

        verify(productoService).actualizarStockProducto(any(Integer.class), any(ActualizarStockRequestDTO.class));
    }

    @Test
    void eliminarProducto_debeRetornarNoContent() throws Exception {
        doNothing().when(productoService).eliminarProducto(1);

        mockMvc.perform(delete("/api/productos/{id}", 1))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productoService).eliminarProducto(1);
    }

    @Test
    void eliminarProducto_debeRetornarError409CuandoHayConflictoReferencial() throws Exception {
        doThrow(new DataIntegrityViolationException("No se puede eliminar el producto porque esta referenciado en un ingreso."))
                .when(productoService).eliminarProducto(1);

        mockMvc.perform(delete("/api/productos/{id}", 1))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("No se puede eliminar el producto porque esta referenciado en un ingreso."))
                .andExpect(jsonPath("$.path").value("/api/productos/1"));

        verify(productoService).eliminarProducto(1);
    }
}
