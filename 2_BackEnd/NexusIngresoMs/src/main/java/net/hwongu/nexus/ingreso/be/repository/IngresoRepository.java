package net.hwongu.nexus.ingreso.be.repository;

import net.hwongu.nexus.ingreso.be.entity.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data para {@link Ingreso}.
 *
 * <p>Al extender {@link JpaRepository}, Spring Boot genera automaticamente la
 * implementacion en tiempo de ejecucion. Esto elimina el codigo JDBC manual y
 * permite que la capa de servicio se concentre en reglas de negocio.</p>
 *
 * @author Henry Wong
 */
@Repository
public interface IngresoRepository extends JpaRepository<Ingreso, Integer> {

    /**
     * Lista los ingresos ordenados por su clave primaria en orden descendente,
     * replicando el comportamiento del sistema anterior.
     *
     * @return ingresos ordenados de mas reciente a mas antiguo.
     */
    List<Ingreso> findAllByOrderByIdIngresoDesc();
}
