package net.hwongu.nexus.catalogo.be.repository;

import net.hwongu.nexus.catalogo.be.entity.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accede a datos de productos.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findAllByOrderByIdProductoAsc();

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findById(Integer id);
}
