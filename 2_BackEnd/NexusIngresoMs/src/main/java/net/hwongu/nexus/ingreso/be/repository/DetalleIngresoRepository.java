package net.hwongu.nexus.ingreso.be.repository;

import net.hwongu.nexus.ingreso.be.entity.DetalleIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Accede a datos de detalles de ingreso.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@Repository
public interface DetalleIngresoRepository extends JpaRepository<DetalleIngreso, Integer> {

    List<DetalleIngreso> findAllByIngresoIdIngresoOrderByIdDetalleAsc(Integer idIngreso);
}
