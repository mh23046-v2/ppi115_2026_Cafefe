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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 *
 * @author hernandez
 */
@Entity
@Table(name = "factura_orden_producto")
@NamedQueries({
    @NamedQuery(name = "FacturaOrdenProducto.findAll", query = "SELECT f FROM FacturaOrdenProducto f"),
    @NamedQuery(name = "FacturaOrdenProducto.count", query = "SELECT COUNT(f) FROM FacturaOrdenProducto f"),
    @NamedQuery(name = "FacturaOrdenProducto.findByFactura", query = "SELECT f FROM FacturaOrdenProducto f JOIN FETCH f.idOrdenProducto op LEFT JOIN FETCH op.idProducto p WHERE f.idFactura = :factura ORDER BY p.nombre, f.idFacturaOrdenProducto"),
    @NamedQuery(name = "FacturaOrdenProducto.sumPrecioByFactura", query = "SELECT SUM(f.precio) FROM FacturaOrdenProducto f WHERE f.idFactura = :factura"),
    @NamedQuery(name = "FacturaOrdenProducto.countVigentesByOrdenProducto", query = "SELECT COUNT(f) FROM FacturaOrdenProducto f WHERE f.idOrdenProducto = :ordenProducto AND (f.idFactura.estado IS NULL OR f.idFactura.estado <> :anulada)"),
    @NamedQuery(name = "FacturaOrdenProducto.deleteByFactura", query = "DELETE FROM FacturaOrdenProducto f WHERE f.idFactura.idFactura = :idFactura"),
    @NamedQuery(name = "FacturaOrdenProducto.countByFactura", query = "SELECT COUNT(f) FROM FacturaOrdenProducto f WHERE f.idFactura = :factura"),
    @NamedQuery(name = "FacturaOrdenProducto.findByOrdenProducto", query = "SELECT f FROM FacturaOrdenProducto f WHERE f.idOrdenProducto = :ordenProducto"),
    @NamedQuery(name = "FacturaOrdenProducto.countByOrdenProducto", query = "SELECT COUNT(f) FROM FacturaOrdenProducto f WHERE f.idOrdenProducto = :ordenProducto"),
    @NamedQuery(name = "FacturaOrdenProducto.findByPrecio", query = "SELECT f FROM FacturaOrdenProducto f WHERE f.precio = :precio"),
    @NamedQuery(name = "FacturaOrdenProducto.findByObservaciones", query = "SELECT f FROM FacturaOrdenProducto f WHERE f.observaciones = :observaciones")})
public class FacturaOrdenProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Convert(converter = UUIDConverter.class)
    @Column(name = "id_factura_orden_producto")
    private UUID idFacturaOrdenProducto;
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Digits(integer = 6, fraction = 2, message = "El precio admite hasta 6 enteros y 2 decimales")
    @Column(name = "precio")
    private BigDecimal precio;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @NotNull(message = "El detalle debe pertenecer a una factura")
    @JoinColumn(name = "id_factura", referencedColumnName = "id_factura")
    @ManyToOne(fetch = FetchType.LAZY)
    private Factura idFactura;
    @NotNull(message = "Seleccione el producto de una orden")
    @JoinColumn(name = "id_orden_producto", referencedColumnName = "id_orden_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private OrdenProducto idOrdenProducto;

    public FacturaOrdenProducto() {
    }

    public FacturaOrdenProducto(UUID idFacturaOrdenProducto) {
        this.idFacturaOrdenProducto = idFacturaOrdenProducto;
    }

    public UUID getIdFacturaOrdenProducto() {
        return idFacturaOrdenProducto;
    }

    public void setIdFacturaOrdenProducto(UUID idFacturaOrdenProducto) {
        this.idFacturaOrdenProducto = idFacturaOrdenProducto;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Factura getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Factura idFactura) {
        this.idFactura = idFactura;
    }

    public OrdenProducto getIdOrdenProducto() {
        return idOrdenProducto;
    }

    public void setIdOrdenProducto(OrdenProducto idOrdenProducto) {
        this.idOrdenProducto = idOrdenProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idFacturaOrdenProducto != null ? idFacturaOrdenProducto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FacturaOrdenProducto)) {
            return false;
        }
        FacturaOrdenProducto other = (FacturaOrdenProducto) object;
        if ((this.idFacturaOrdenProducto == null && other.idFacturaOrdenProducto != null) || (this.idFacturaOrdenProducto != null && !this.idFacturaOrdenProducto.equals(other.idFacturaOrdenProducto))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.ingenieria.pos.ppi115_2026.cafefe.entity.FacturaOrdenProducto[ idFacturaOrdenProducto=" + idFacturaOrdenProducto + " ]";
    }
    
}
