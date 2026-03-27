package net.hwongu.nexus.seguridad.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que representa la tabla {@code usuario}.
 *
 * <p>La anotacion {@link Entity} le indica a Spring Data JPA que esta clase
 * debe persistirse en la base de datos. {@link Table} se utiliza para enlazar
 * explicitamente la clase con la tabla real de PostgreSQL, lo cual es util
 * cuando en clase se desea mostrar que el nombre del objeto Java no siempre
 * coincide con el nombre fisico de la tabla.</p>
 *
 * <p>Se usa Lombok para reducir codigo repetitivo y permitir que los
 * estudiantes se concentren en la arquitectura del microservicio, la
 * persistencia y la logica de negocio.</p>
 *
 * @author Henry Wong
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    /**
     * Clave primaria del usuario.
     *
     * <p>{@link Id} marca el identificador unico de la entidad y
     * {@link GeneratedValue} con estrategia {@link GenerationType#IDENTITY}
     * delega a PostgreSQL la generacion autonumerica del valor.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    /**
     * Nombre de usuario utilizado para autenticacion.
     */
    @Column(name = "username", nullable = false, length = 120)
    private String username;

    /**
     * Contrasena persistida del usuario.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Estado de activacion del usuario.
     */
    @Column(name = "estado", nullable = false)
    private Boolean estado;
}
