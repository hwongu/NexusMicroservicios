package net.hwongu.nexus.ingreso.be.repository;

import net.hwongu.nexus.ingreso.be.entity.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Accede a datos de ingresos.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@Repository
public interface IngresoRepository extends JpaRepository<Ingreso, Integer> {

    List<Ingreso> findAllByOrderByIdIngresoDesc();
}
