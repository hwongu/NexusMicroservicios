package net.hwongu.nexus.catalogo.be.service;

import net.hwongu.nexus.catalogo.be.dto.ActualizarStockRequestDTO;
import net.hwongu.nexus.catalogo.be.dto.ProductoDTO;
import net.hwongu.nexus.catalogo.be.entity.Categoria;
import net.hwongu.nexus.catalogo.be.entity.Producto;
import net.hwongu.nexus.catalogo.be.exception.BadRequestException;
import net.hwongu.nexus.catalogo.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.catalogo.be.repository.CategoriaRepository;
import net.hwongu.nexus.catalogo.be.repository.ProductoRepository;
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
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void listarProductos_debeRetornarProductosConvertidosADto() {
        Categoria categoria = crearCategoria(1, "Laptops");
        Producto producto1 = crearProducto(1, categoria, "ThinkPad T14", 1200.5, 50);
        Producto producto2 = crearProducto(2, categoria, "ThinkPad X1", 1800.0, 20);
        when(productoRepository.findAllByOrderByIdProductoAsc()).thenReturn(List.of(producto1, producto2));

        List<ProductoDTO> resultado = productoService.listarProductos();

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getIdProducto());
        assertEquals("Laptops", resultado.get(0).getNombreCategoria());
        assertEquals("ThinkPad X1", resultado.get(1).getNombre());
        verify(productoRepository).findAllByOrderByIdProductoAsc();
    }

    @Test
    void listarProductos_debeRetornarListaVaciaCuandoNoHayRegistros() {
        when(productoRepository.findAllByOrderByIdProductoAsc()).thenReturn(List.of());

        List<ProductoDTO> resultado = productoService.listarProductos();

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
        verify(productoRepository).findAllByOrderByIdProductoAsc();
    }

    @Test
    void buscarProductoPorId_debeRetornarProductoCuandoExiste() {
        Categoria categoria = crearCategoria(1, "Laptops");
        Producto producto = crearProducto(7, categoria, "ThinkPad T14", 1200.5, 50);
        when(productoRepository.findById(7)).thenReturn(Optional.of(producto));

        ProductoDTO resultado = productoService.buscarProductoPorId(7);

        assertEquals(7, resultado.getIdProducto());
        assertEquals(1, resultado.getIdCategoria());
        assertEquals("Laptops", resultado.getNombreCategoria());
        assertEquals("ThinkPad T14", resultado.getNombre());
        verify(productoRepository).findById(7);
    }

    @Test
    void buscarProductoPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.buscarProductoPorId(99)
        );

        assertEquals("Producto no encontrado.", exception.getMessage());
        verify(productoRepository).findById(99);
    }

    @Test
    void registrarProducto_debeGuardarProductoCuandoCategoriaExiste() {
        ProductoDTO request = ProductoDTO.builder()
                .idProducto(40)
                .idCategoria(1)
                .nombre("ThinkPad T14")
                .precio(1200.5)
                .stock(50)
                .build();
        Categoria categoria = crearCategoria(1, "Laptops");
        Producto productoGuardado = crearProducto(5, categoria, "ThinkPad T14", 1200.5, 50);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        ProductoDTO resultado = productoService.registrarProducto(request);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        Producto productoEnviadoAGuardar = captor.getValue();

        assertNull(productoEnviadoAGuardar.getIdProducto());
        assertEquals("ThinkPad T14", productoEnviadoAGuardar.getNombre());
        assertEquals(categoria, productoEnviadoAGuardar.getCategoria());
        assertEquals(5, resultado.getIdProducto());
        assertEquals("Laptops", resultado.getNombreCategoria());
    }

    @Test
    void registrarProducto_debeLanzarBadRequestCuandoCategoriaNoExiste() {
        ProductoDTO request = ProductoDTO.builder()
                .idCategoria(99)
                .nombre("ThinkPad T14")
                .precio(1200.5)
                .stock(50)
                .build();
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productoService.registrarProducto(request)
        );

        assertEquals("La categoria indicada no existe.", exception.getMessage());
        verify(categoriaRepository).findById(99);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_debeActualizarDatosCuandoProductoYCategoriaExisten() {
        Categoria categoriaActual = crearCategoria(1, "Laptops");
        Categoria categoriaNueva = crearCategoria(2, "Monitores");
        Producto productoExistente = crearProducto(3, categoriaActual, "ThinkPad T14", 1200.5, 50);
        ProductoDTO request = ProductoDTO.builder()
                .idCategoria(2)
                .nombre("Monitor Ultrawide")
                .precio(999.0)
                .stock(15)
                .build();
        when(productoRepository.findById(3)).thenReturn(Optional.of(productoExistente));
        when(categoriaRepository.findById(2)).thenReturn(Optional.of(categoriaNueva));

        productoService.actualizarProducto(3, request);

        verify(productoRepository).save(productoExistente);
        assertEquals(categoriaNueva, productoExistente.getCategoria());
        assertEquals("Monitor Ultrawide", productoExistente.getNombre());
        assertEquals(999.0, productoExistente.getPrecio());
        assertEquals(15, productoExistente.getStock());
    }

    @Test
    void actualizarProducto_debeLanzarExcepcionCuandoProductoNoExiste() {
        ProductoDTO request = ProductoDTO.builder()
                .idCategoria(1)
                .nombre("Monitor Ultrawide")
                .precio(999.0)
                .stock(15)
                .build();
        when(productoRepository.findById(3)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.actualizarProducto(3, request)
        );

        assertEquals("Producto no encontrado.", exception.getMessage());
        verify(productoRepository).findById(3);
        verify(categoriaRepository, never()).findById(org.mockito.ArgumentMatchers.anyInt());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_debeLanzarBadRequestCuandoCategoriaNoExiste() {
        Categoria categoriaActual = crearCategoria(1, "Laptops");
        Producto productoExistente = crearProducto(3, categoriaActual, "ThinkPad T14", 1200.5, 50);
        ProductoDTO request = ProductoDTO.builder()
                .idCategoria(9)
                .nombre("Monitor Ultrawide")
                .precio(999.0)
                .stock(15)
                .build();
        when(productoRepository.findById(3)).thenReturn(Optional.of(productoExistente));
        when(categoriaRepository.findById(9)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productoService.actualizarProducto(3, request)
        );

        assertEquals("La categoria indicada no existe.", exception.getMessage());
        verify(productoRepository).findById(3);
        verify(categoriaRepository).findById(9);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizarStockProducto_debeSumarStockCuandoOperacionEsSumar() {
        Categoria categoria = crearCategoria(1, "Laptops");
        Producto productoExistente = crearProducto(8, categoria, "ThinkPad T14", 1200.5, 50);
        ActualizarStockRequestDTO request = ActualizarStockRequestDTO.builder()
                .cantidad(10)
                .operacion(" sumar ")
                .build();
        when(productoRepository.findById(8)).thenReturn(Optional.of(productoExistente));

        productoService.actualizarStockProducto(8, request);

        assertEquals(60, productoExistente.getStock());
        verify(productoRepository).save(productoExistente);
    }

    @Test
    void actualizarStockProducto_debeRestarStockCuandoOperacionEsRestar() {
        Categoria categoria = crearCategoria(1, "Laptops");
        Producto productoExistente = crearProducto(8, categoria, "ThinkPad T14", 1200.5, 50);
        ActualizarStockRequestDTO request = ActualizarStockRequestDTO.builder()
                .cantidad(5)
                .operacion("RESTAR")
                .build();
        when(productoRepository.findById(8)).thenReturn(Optional.of(productoExistente));

        productoService.actualizarStockProducto(8, request);

        assertEquals(45, productoExistente.getStock());
        verify(productoRepository).save(productoExistente);
    }

    @Test
    void actualizarStockProducto_debeLanzarExcepcionCuandoProductoNoExiste() {
        ActualizarStockRequestDTO request = ActualizarStockRequestDTO.builder()
                .cantidad(5)
                .operacion("SUMAR")
                .build();
        when(productoRepository.findById(8)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.actualizarStockProducto(8, request)
        );

        assertEquals("Producto no encontrado.", exception.getMessage());
        verify(productoRepository).findById(8);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizarStockProducto_debeLanzarBadRequestCuandoOperacionEsInvalida() {
        Categoria categoria = crearCategoria(1, "Laptops");
        Producto productoExistente = crearProducto(8, categoria, "ThinkPad T14", 1200.5, 50);
        ActualizarStockRequestDTO request = ActualizarStockRequestDTO.builder()
                .cantidad(5)
                .operacion("DIVIDIR")
                .build();
        when(productoRepository.findById(8)).thenReturn(Optional.of(productoExistente));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productoService.actualizarStockProducto(8, request)
        );

        assertEquals("La operacion debe ser SUMAR o RESTAR.", exception.getMessage());
        verify(productoRepository).findById(8);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizarStockProducto_debeLanzarErrorCuandoStockQuedaNegativo() {
        Categoria categoria = crearCategoria(1, "Laptops");
        Producto productoExistente = crearProducto(8, categoria, "ThinkPad T14", 1200.5, 3);
        ActualizarStockRequestDTO request = ActualizarStockRequestDTO.builder()
                .cantidad(5)
                .operacion("RESTAR")
                .build();
        when(productoRepository.findById(8)).thenReturn(Optional.of(productoExistente));

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> productoService.actualizarStockProducto(8, request)
        );

        assertEquals(
                "No se puede restar el stock porque el producto quedaria con stock negativo.",
                exception.getMessage()
        );
        verify(productoRepository).findById(8);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarProducto_debeEliminarYHacerFlushCuandoExiste() {
        when(productoRepository.existsById(4)).thenReturn(true);

        productoService.eliminarProducto(4);

        verify(productoRepository).existsById(4);
        verify(productoRepository).deleteById(4);
        verify(productoRepository).flush();
    }

    @Test
    void eliminarProducto_debeLanzarExcepcionCuandoNoExiste() {
        when(productoRepository.existsById(12)).thenReturn(false);

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> productoService.eliminarProducto(12)
        );

        assertEquals("Producto no encontrado.", exception.getMessage());
        verify(productoRepository).existsById(12);
        verify(productoRepository, never()).deleteById(12);
        verify(productoRepository, never()).flush();
    }

    @Test
    void eliminarProducto_debeTraducirErrorDeIntegridadReferencial() {
        when(productoRepository.existsById(4)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(productoRepository).flush();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> productoService.eliminarProducto(4)
        );

        assertEquals(
                "No se puede eliminar el producto porque esta referenciado en un ingreso.",
                exception.getMessage()
        );
        verify(productoRepository).deleteById(4);
        verify(productoRepository).flush();
    }

    private Categoria crearCategoria(Integer id, String nombre) {
        return Categoria.builder()
                .idCategoria(id)
                .nombre(nombre)
                .descripcion("Descripcion")
                .build();
    }

    private Producto crearProducto(Integer id, Categoria categoria, String nombre, Double precio, Integer stock) {
        return Producto.builder()
                .idProducto(id)
                .categoria(categoria)
                .nombre(nombre)
                .precio(precio)
                .stock(stock)
                .build();
    }
}
