package net.hwongu.nexus.catalogo.be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hwongu.nexus.catalogo.be.dto.ActualizarStockRequestDTO;
import net.hwongu.nexus.catalogo.be.dto.ProductoDTO;
import net.hwongu.nexus.catalogo.be.service.ProductoService;
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
 * Controlador REST del recurso productos.
 *
 * <p>Este controlador traslada la logica HTTP del monolito a Spring MVC,
 * aprovechando anotaciones declarativas como {@link GetMapping} o
 * {@link PostMapping}, que hacen el codigo mas expresivo y mantenible para los
 * estudiantes.</p>
 *
 * @author Henry Wong
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Lista todos los productos del catalogo.
     *
     * @return lista de productos con su categoria.
     */
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        return ResponseEntity.ok(productoService.listarProductos());
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id identificador solicitado.
     * @return producto encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarProductoPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.buscarProductoPorId(id));
    }

    /**
     * Registra un producto nuevo.
     *
     * @param productoDTO datos enviados por el cliente.
     * @return producto creado con HTTP 201.
     */
    @PostMapping
    public ResponseEntity<ProductoDTO> registrarProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO productoCreado = productoService.registrarProducto(productoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
    }

    /**
     * Actualiza un producto existente.
     *
     * @param id identificador del producto.
     * @param productoDTO nuevos datos del producto.
     * @return mensaje de exito.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoDTO productoDTO
    ) {
        productoService.actualizarProducto(id, productoDTO);
        return ResponseEntity.ok(Map.of("message", "Producto actualizado exitosamente"));
    }

    /**
     * Ajusta el stock de un producto mediante una operacion simple.
     *
     * <p>Este endpoint se expone para que otros microservicios, como ingresos,
     * puedan reflejar movimientos de stock sin reenviar el producto completo.</p>
     *
     * @param id identificador del producto.
     * @param requestDTO cantidad y tipo de operacion solicitada.
     * @return mensaje de confirmacion.
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<Map<String, String>> actualizarStockProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarStockRequestDTO requestDTO
    ) {
        productoService.actualizarStockProducto(id, requestDTO);
        return ResponseEntity.ok(Map.of("message", "Stock del producto actualizado exitosamente"));
    }

    /**
     * Elimina un producto por su identificador.
     *
     * @param id identificador del producto.
     * @return 204 si la operacion se realiza.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
