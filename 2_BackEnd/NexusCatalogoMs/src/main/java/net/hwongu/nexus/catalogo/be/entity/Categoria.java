package net.hwongu.nexus.catalogo.be.entity;

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
 * Entidad JPA que representa la tabla {@code categoria}.
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
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    /**
     * Clave primaria de la categoria.
     *
     * <p>{@link Id} marca el identificador unico de la entidad y
     * {@link GeneratedValue} con estrategia {@link GenerationType#IDENTITY}
     * delega a PostgreSQL la generacion autonumerica del valor.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    /**
     * Nombre funcional de la categoria.
     */
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    /**
     * Descripcion libre de apoyo para usuarios o administradores.
     */
    @Column(name = "descripcion", length = 255)
    private String descripcion;
}
