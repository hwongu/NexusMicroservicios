package net.hwongu.nexus.catalogo.be.repository;

import net.hwongu.nexus.catalogo.be.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data para {@link Categoria}.
 *
 * <p>Al extender {@link JpaRepository}, Spring Boot genera automaticamente la
 * implementacion en tiempo de ejecucion. Esto elimina el codigo JDBC manual y
 * permite que la capa de servicio se concentre en reglas de negocio.</p>
 *
 * @author Henry Wong
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    /**
     * Lista las categorias ordenadas por su clave primaria, replicando el
     * comportamiento del monolito original.
     *
     * @return categorias ordenadas ascendentemente.
     */
    List<Categoria> findAllByOrderByIdCategoriaAsc();
}
