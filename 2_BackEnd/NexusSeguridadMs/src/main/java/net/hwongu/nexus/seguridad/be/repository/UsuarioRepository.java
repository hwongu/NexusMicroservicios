package net.hwongu.nexus.seguridad.be.repository;

import net.hwongu.nexus.seguridad.be.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data para {@link Usuario}.
 *
 * <p>Al extender {@link JpaRepository}, Spring Boot genera automaticamente la
 * implementacion en tiempo de ejecucion. Esto elimina el codigo JDBC manual y
 * permite que la capa de servicio se concentre en reglas de negocio.</p>
 *
 * @author Henry Wong
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Lista los usuarios ordenados por su clave primaria, replicando el
     * comportamiento del sistema anterior.
     *
     * @return usuarios ordenados ascendentemente.
     */
    List<Usuario> findAllByOrderByIdUsuarioAsc();

    /**
     * Busca un usuario activo por sus credenciales.
     *
     * @param username nombre de usuario.
     * @param password contrasena enviada por el cliente.
     * @return usuario encontrado, si las credenciales son validas.
     */
    Optional<Usuario> findByUsernameAndPasswordAndEstadoTrue(String username, String password);
}
