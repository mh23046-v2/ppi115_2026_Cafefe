package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author hernandez
 */
@Entity
@Table(name = "producto_tipo_producto")
@NamedQueries({
    @NamedQuery(name = "ProductoTipoProducto.findAll", query = "SELECT p FROM ProductoTipoProducto p"),
    @NamedQuery(name = "ProductoTipoProducto.findByFechaCreacion", query = "SELECT p FROM ProductoTipoProducto p WHERE p.fechaCreacion = :fechaCreacion"),
    @NamedQuery(name = "ProductoTipoProducto.findByObservaciones", query = "SELECT p FROM ProductoTipoProducto p WHERE p.observaciones = :observaciones")})
public class ProductoTipoProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Convert(converter = UUIDConverter.class)
    @Column(name = "id_producto_tipo_producto")
    private UUID idProductoTipoProducto;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto producto;
    @JoinColumn(name = "id_tipo_producto", referencedColumnName = "id_tipo_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private TipoProducto idTipoProducto;

    public ProductoTipoProducto() {
    }

    public ProductoTipoProducto(UUID idProductoTipoProducto) {
        this.idProductoTipoProducto = idProductoTipoProducto;
    }

    public UUID getIdProductoTipoProducto() {
        return idProductoTipoProducto;
    }

    public void setIdProductoTipoProducto(UUID idProductoTipoProducto) {
        this.idProductoTipoProducto = idProductoTipoProducto;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public TipoProducto getIdTipoProducto() {
        return idTipoProducto;
    }

    public void setIdTipoProducto(TipoProducto idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProductoTipoProducto != null ? idProductoTipoProducto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ProductoTipoProducto)) {
            return false;
        }
        ProductoTipoProducto other = (ProductoTipoProducto) object;
        if ((this.idProductoTipoProducto == null && other.idProductoTipoProducto != null) || (this.idProductoTipoProducto != null && !this.idProductoTipoProducto.equals(other.idProductoTipoProducto))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.ingenieria.pos.ppi115_2026.cafefe.entity.ProductoTipoProducto[ idProductoTipoProducto=" + idProductoTipoProducto + " ]";
    }

}