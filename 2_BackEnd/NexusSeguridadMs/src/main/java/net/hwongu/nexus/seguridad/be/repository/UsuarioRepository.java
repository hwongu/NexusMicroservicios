package net.hwongu.nexus.seguridad.be.repository;

import net.hwongu.nexus.seguridad.be.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accede a datos de usuarios.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByOrderByIdUsuarioAsc();

    Optional<Usuario> findByUsernameAndPasswordAndEstadoTrue(String username, String password);
}
