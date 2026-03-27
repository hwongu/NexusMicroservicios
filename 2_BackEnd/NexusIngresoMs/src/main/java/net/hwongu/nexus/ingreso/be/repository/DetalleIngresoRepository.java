package net.hwongu.nexus.ingreso.be.repository;

import net.hwongu.nexus.ingreso.be.entity.DetalleIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data para {@link DetalleIngreso}.
 *
 * @author Henry Wong
 */
@Repository
public interface DetalleIngresoRepository extends JpaRepository<DetalleIngreso, Integer> {

    /**
     * Recupera todos los detalles de un ingreso especifico.
     *
     * @param idIngreso identificador del ingreso padre.
     * @return detalles asociados en orden ascendente.
     */
    List<DetalleIngreso> findAllByIngresoIdIngresoOrderByIdDetalleAsc(Integer idIngreso);
}
