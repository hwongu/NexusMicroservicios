package net.hwongu.nexus.catalogo.be.service;

import lombok.RequiredArgsConstructor;
import net.hwongu.nexus.catalogo.be.dto.ActualizarStockRequestDTO;
import net.hwongu.nexus.catalogo.be.dto.ProductoDTO;
import net.hwongu.nexus.catalogo.be.entity.Categoria;
import net.hwongu.nexus.catalogo.be.entity.Producto;
import net.hwongu.nexus.catalogo.be.exception.BadRequestException;
import net.hwongu.nexus.catalogo.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.catalogo.be.repository.CategoriaRepository;
import net.hwongu.nexus.catalogo.be.repository.ProductoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Capa de servicio para operaciones de productos.
 *
 * <p>Ademas de delegar la persistencia al repositorio, este servicio valida
 * que la categoria exista antes de registrar o actualizar un producto. Esa
 * verificacion es pedagogicamente importante porque muestra que la capa de
 * servicio protege las reglas del dominio antes de llegar a la base.</p>
 *
 * @author Henry Wong
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    /**
     * Lista todos los productos con los datos de su categoria.
     *
     * @return lista de DTOs ordenada por ID.
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAllByOrderByIdProductoAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    /**
     * Busca un producto por su identificador.
     *
     * @param id identificador del producto.
     * @return DTO enriquecido con la informacion de la categoria.
     */
    @Transactional(readOnly = true)
    public ProductoDTO buscarProductoPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
        return convertirADTO(producto);
    }

    /**
     * Registra un nuevo producto.
     *
     * @param productoDTO datos recibidos desde la API.
     * @return producto registrado con ID generado.
     */
    @Transactional
    public ProductoDTO registrarProducto(ProductoDTO productoDTO) {
        Categoria categoria = obtenerCategoriaExistente(productoDTO.getIdCategoria());

        Producto producto = convertirAEntidad(productoDTO, categoria);
        producto.setIdProducto(null);

        Producto productoGuardado = productoRepository.save(producto);
        return convertirADTO(productoGuardado);
    }

    /**
     * Actualiza un producto existente.
     *
     * @param id identificador del producto a modificar.
     * @param productoDTO nuevos datos recibidos desde la API.
     */
    @Transactional
    public void actualizarProducto(Integer id, ProductoDTO productoDTO) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));

        Categoria categoria = obtenerCategoriaExistente(productoDTO.getIdCategoria());

        productoExistente.setCategoria(categoria);
        productoExistente.setNombre(productoDTO.getNombre());
        productoExistente.setPrecio(productoDTO.getPrecio());
        productoExistente.setStock(productoDTO.getStock());

        productoRepository.save(productoExistente);
    }

    /**
     * Ajusta el stock de un producto usando una operacion simple de sumar o
     * restar.
     *
     * @param id identificador del producto.
     * @param requestDTO cantidad y operacion solicitada.
     */
    @Transactional
    public void actualizarStockProducto(Integer id, ActualizarStockRequestDTO requestDTO) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));

        String operacion = requestDTO.getOperacion().trim().toUpperCase();
        Integer cantidad = requestDTO.getCantidad();

        if (!"SUMAR".equals(operacion) && !"RESTAR".equals(operacion)) {
            throw new BadRequestException("La operacion debe ser SUMAR o RESTAR.");
        }

        if ("SUMAR".equals(operacion)) {
            productoExistente.setStock(productoExistente.getStock() + cantidad);
        } else {
            int nuevoStock = productoExistente.getStock() - cantidad;

            if (nuevoStock < 0) {
                throw new DataIntegrityViolationException(
                        "No se puede restar el stock porque el producto quedaria con stock negativo."
                );
            }

            productoExistente.setStock(nuevoStock);
        }

        productoRepository.save(productoExistente);
    }

    /**
     * Elimina un producto si existe y si no esta referenciado por otras tablas.
     *
     * @param id identificador del producto.
     */
    @Transactional
    public void eliminarProducto(Integer id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado.");
        }

        try {
            productoRepository.deleteById(id);
            productoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "No se puede eliminar el producto porque esta referenciado en un ingreso.",
                    e
            );
        }
    }

    /**
     * Convierte una entidad a DTO.
     *
     * @param producto entidad persistida.
     * @return DTO listo para respuesta.
     */
    private ProductoDTO convertirADTO(Producto producto) {
        return ProductoDTO.builder()
                .idProducto(producto.getIdProducto())
                .idCategoria(producto.getCategoria().getIdCategoria())
                .nombreCategoria(producto.getCategoria().getNombre())
                .nombre(producto.getNombre())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .build();
    }

    /**
     * Convierte un DTO a entidad usando una categoria ya validada.
     *
     * @param productoDTO datos del producto.
     * @param categoria categoria existente en base de datos.
     * @return entidad lista para persistir.
     */
    private Producto convertirAEntidad(ProductoDTO productoDTO, Categoria categoria) {
        return Producto.builder()
                .idProducto(productoDTO.getIdProducto())
                .categoria(categoria)
                .nombre(productoDTO.getNombre())
                .precio(productoDTO.getPrecio())
                .stock(productoDTO.getStock())
                .build();
    }

    /**
     * Recupera una categoria valida o lanza una excepcion de negocio si no
     * existe.
     *
     * @param idCategoria identificador de la categoria.
     * @return entidad de categoria gestionada por JPA.
     */
    private Categoria obtenerCategoriaExistente(Integer idCategoria) {
        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new BadRequestException("La categoria indicada no existe."));
    }
}
