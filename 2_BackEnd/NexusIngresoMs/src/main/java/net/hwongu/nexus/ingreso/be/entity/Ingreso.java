package net.hwongu.nexus.ingreso.be.entity;

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

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la tabla {@code ingreso}.
 *
 * <p>El ingreso almacena solo el {@code idUsuario} y no una relacion JPA hacia
 * seguridad, porque en una arquitectura de microservicios cada servicio
 * persiste exclusivamente sus propios datos.</p>
 *
 * @author Henry Wong
 */
@Entity
@Table(name = "ingreso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingreso {

    /**
     * Clave primaria del ingreso.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso")
    private Integer idIngreso;

    /**
     * Identificador del usuario que registro el ingreso.
     */
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    /**
     * Fecha y hora en que se registro el ingreso.
     */
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    /**
     * Estado actual del ingreso.
     */
    @Column(name = "estado", nullable = false, length = 50)
    private String estado;
}
