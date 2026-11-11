package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "producto_caracteristica")
@NamedQueries({
    @NamedQuery(name = "ProductoCaracteristica.findAll", query = "SELECT pc FROM ProductoCaracteristica pc"),
    @NamedQuery(name = "ProductoCaracteristica.findByProducto", query = "SELECT pc FROM ProductoCaracteristica pc JOIN FETCH pc.idCaracteristica c JOIN FETCH c.idTipoCaracteristica WHERE pc.idProducto = :producto ORDER BY c.nombre ASC, pc.idProductoCaracteristica ASC"),
    @NamedQuery(name = "ProductoCaracteristica.countByProducto", query = "SELECT COUNT(pc) FROM ProductoCaracteristica pc WHERE pc.idProducto = :producto")})
public class ProductoCaracteristica implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Convert(converter = UUIDConverter.class)
    @Column(name = "id_producto_caracteristica")
    private UUID idProductoCaracteristica;

    @Size(max = 2147483647)
    @Column(name = "valor")
    private String valor;

    @JoinColumn(name = "id_caracteristica", referencedColumnName = "id_caracteristica")
    @ManyToOne(fetch = FetchType.LAZY)
    private Caracteristica idCaracteristica;
    
    @JsonbTransient
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    @ManyToOne(fetch = FetchType.LAZY)
    private Producto idProducto;

    public ProductoCaracteristica() {
    }

    public ProductoCaracteristica(UUID idProductoCaracteristica) {
        this.idProductoCaracteristica = idProductoCaracteristica;
    }

    public UUID getIdProductoCaracteristica() {
        return idProductoCaracteristica;
    }

    public void setIdProductoCaracteristica(UUID idProductoCaracteristica) {
        this.idProductoCaracteristica = idProductoCaracteristica;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Caracteristica getIdCaracteristica() {
        return idCaracteristica;
    }

    public void setIdCaracteristica(Caracteristica idCaracteristica) {
        this.idCaracteristica = idCaracteristica;
    }

    public Producto getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProductoCaracteristica != null ? idProductoCaracteristica.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProductoCaracteristica)) {
            return false;
        }
        ProductoCaracteristica other = (ProductoCaracteristica) object;
        if ((this.idProductoCaracteristica == null && other.idProductoCaracteristica != null) || (this.idProductoCaracteristica != null && !this.idProductoCaracteristica.equals(other.idProductoCaracteristica))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.ingenieria.pos.ppi115_2026.cafefe.entity.ProductoCaracteristica[ idProductoCaracteristica=" + idProductoCaracteristica + " ]";
    }

}
