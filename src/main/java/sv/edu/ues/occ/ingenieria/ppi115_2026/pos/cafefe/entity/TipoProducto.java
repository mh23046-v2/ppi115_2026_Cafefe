package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author hernandez
 */
@Entity
@Table(name = "tipo_producto")
@NamedQueries({
    @NamedQuery(name = "TipoProducto.findAll", query = "SELECT t FROM TipoProducto t"),
    @NamedQuery(name = "TipoProducto.findByNombre", query = "SELECT t FROM TipoProducto t WHERE t.nombre = :nombre"),
    @NamedQuery(name = "TipoProducto.findByActivo", query = "SELECT t FROM TipoProducto t WHERE t.activo = :activo"),
    @NamedQuery(name = "TipoProducto.findByObservaciones", query = "SELECT t FROM TipoProducto t WHERE t.observaciones = :observaciones")})
public class TipoProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Convert(converter = UUIDConverter.class)
    @Column(name = "id_tipo_producto")
    private UUID idTipoProducto;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 155)
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(mappedBy = "idTipoProducto", fetch = FetchType.LAZY)
    private List<ProductoTipoProducto> productoTipoProductoList;

    public TipoProducto() {
    }

    public TipoProducto(UUID idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    public UUID getIdTipoProducto() {
        return idTipoProducto;
    }

    public void setIdTipoProducto(UUID idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<ProductoTipoProducto> getProductoTipoProductoList() {
        return productoTipoProductoList;
    }

    public void setProductoTipoProductoList(List<ProductoTipoProducto> productoTipoProductoList) {
        this.productoTipoProductoList = productoTipoProductoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoProducto != null ? idTipoProducto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoProducto)) {
            return false;
        }
        TipoProducto other = (TipoProducto) object;
        if ((this.idTipoProducto == null && other.idTipoProducto != null) || (this.idTipoProducto != null && !this.idTipoProducto.equals(other.idTipoProducto))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.ingenieria.pos.ppi115_2026.cafefe.entity.TipoProducto[ idTipoProducto=" + idTipoProducto + " ]";
    }
    
}
