package net.hwongu.nexus.catalogo.be.repository;

import net.hwongu.nexus.catalogo.be.entity.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para {@link Producto}.
 *
 * <p>Se utiliza {@link EntityGraph} para indicar a JPA que traiga la
 * categoria asociada junto con cada producto cuando realmente la API la
 * necesita. Esto evita problemas de carga perezosa fuera de la transaccion y
 * reduce consultas adicionales innecesarias.</p>
 *
 * @author Henry Wong
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Obtiene todos los productos junto con su categoria, ordenados por ID.
     *
     * @return lista completa del catalogo de productos.
     */
    @EntityGraph(attributePaths = "categoria")
    List<Producto> findAllByOrderByIdProductoAsc();

    /**
     * Busca un producto por su identificador cargando tambien la categoria.
     *
     * @param id identificador del producto.
     * @return producto encontrado, si existe.
     */
    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findById(Integer id);
}
