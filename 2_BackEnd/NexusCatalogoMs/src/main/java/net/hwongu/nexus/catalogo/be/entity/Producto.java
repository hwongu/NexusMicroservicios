package net.hwongu.nexus.catalogo.be.entity;

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
 * Entidad JPA que representa la tabla {@code producto}.
 *
 * <p>El producto mantiene una relacion {@code muchos-a-uno} con
 * {@link Categoria}, porque muchos productos pueden pertenecer a una misma
 * categoria. Esta relacion reemplaza el manejo manual del {@code JOIN} que
 * hacia el monolito mediante JDBC.</p>
 *
 * @author Henry Wong
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    /**
     * Clave primaria del producto.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    /**
     * Categoria a la que pertenece el producto.
     *
     * <p>{@link ManyToOne} define la cardinalidad de la relacion y
     * {@link JoinColumn} especifica la columna FK real en PostgreSQL. Se usa
     * carga perezosa ({@link FetchType#LAZY}) para no traer automaticamente la
     * categoria si no se necesita; el servicio decide cuando materializarla.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    /**
     * Nombre comercial del producto.
     */
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /**
     * Precio unitario de venta.
     */
    @Column(name = "precio", nullable = false)
    private Double precio;

    /**
     * Cantidad de unidades disponibles.
     */
    @Column(name = "stock", nullable = false)
    private Integer stock;
}
