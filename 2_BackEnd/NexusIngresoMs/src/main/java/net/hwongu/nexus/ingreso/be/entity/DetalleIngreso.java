package net.hwongu.nexus.ingreso.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que representa la tabla {@code detalle_ingreso}.
 *
 * <p>El detalle mantiene una relacion local con {@link Ingreso}, pero guarda
 * el {@code idProducto} como dato simple porque el producto pertenece al
 * microservicio de catalogo.</p>
 *
 * @author Henry Wong
 */
@Entity
@Table(name = "detalle_ingreso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleIngreso {

    /**
     * Clave primaria del detalle.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    /**
     * Ingreso al que pertenece el detalle.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ingreso", nullable = false)
    private Ingreso ingreso;

    /**
     * Identificador del producto asociado.
     */
    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;

    /**
     * Cantidad ingresada del producto.
     */
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    /**
     * Precio de compra unitario.
     */
    @Column(name = "precio_compra", nullable = false)
    private Double precioCompra;
}
