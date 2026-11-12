package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.json.bind.annotation.JsonbTransient;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author hernandez
 */
@Entity
@Table(name = "factura")
@NamedQueries({
    @NamedQuery(name = "Factura.findPorCobrar", query = "SELECT f FROM Factura f WHERE f.estado = :pendiente AND LOWER(f.cliente) LIKE :texto AND EXISTS (SELECT fop FROM FacturaOrdenProducto fop WHERE fop.idFactura = f) AND NOT EXISTS (SELECT p FROM Pago p WHERE p.idFactura = f AND p.estado <> :anulado) ORDER BY f.fechaFacturacion DESC, f.idFactura"),
    @NamedQuery(name = "Factura.findAll", query = "SELECT f FROM Factura f"),
    @NamedQuery(name = "Factura.findRecientes", query = "SELECT f FROM Factura f ORDER BY f.fechaFacturacion DESC, f.idFactura"),
    @NamedQuery(name = "Factura.count", query = "SELECT COUNT(f) FROM Factura f"),
    @NamedQuery(name = "Factura.findByEstado", query = "SELECT f FROM Factura f WHERE f.estado = :estado"),
    @NamedQuery(name = "Factura.countByEstado", query = "SELECT COUNT(f) FROM Factura f WHERE f.estado = :estado"),
    @NamedQuery(name = "Factura.findByCaja", query = "SELECT f FROM Factura f WHERE f.idCaja = :caja"),
    @NamedQuery(name = "Factura.countByCaja", query = "SELECT COUNT(f) FROM Factura f WHERE f.idCaja = :caja"),
    @NamedQuery(name = "Factura.findByCliente", query = "SELECT f FROM Factura f WHERE LOWER(f.cliente) LIKE :cliente"),
    @NamedQuery(name = "Factura.countByCliente", query = "SELECT COUNT(f) FROM Factura f WHERE LOWER(f.cliente) LIKE :cliente"),
    @NamedQuery(name = "Factura.findByFechaFacturacion", query = "SELECT f FROM Factura f WHERE f.fechaFacturacion = :fechaFacturacion"),
    @NamedQuery(name = "Factura.findByObservaciones", query = "SELECT f FROM Factura f WHERE f.observaciones = :observaciones")})
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Convert(converter = UUIDConverter.class)    
    @Column(name = "id_factura")
    private UUID idFactura;
    @Size(max = 155, message = "El cliente admite hasta 155 caracteres")
    @Column(name = "cliente")
    private String cliente;
    @NotNull(message = "La fecha de facturación es obligatoria")
    @Column(name = "fecha_facturacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFacturacion;
    @NotNull(message = "El estado es obligatorio")
    @Size(max = 20)
    @Column(name = "estado")
    private String estado;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @JsonbTransient
    @OneToMany(mappedBy = "idFactura", fetch = FetchType.LAZY)
    private List<Pago> pagoList;
    @JsonbTransient
    @OneToMany(mappedBy = "idFactura", fetch = FetchType.LAZY)
    private List<FacturaOrdenProducto> facturaOrdenProductoList;
    @NotNull(message = "Seleccione la caja")
    @JoinColumn(name = "id_caja", referencedColumnName = "id_caja")
    @ManyToOne(fetch = FetchType.LAZY)
    private Caja idCaja;
    @NotNull(message = "Seleccione el empleado que factura")
    @JoinColumn(name = "id_empleado_rol", referencedColumnName = "id_empleado_rol")
    @ManyToOne(fetch = FetchType.LAZY)
    private EmpleadoRol idEmpleadoRol;

    public Factura() {
    }

    public Factura(UUID idFactura) {
        this.idFactura = idFactura;
    }

    public UUID getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(UUID idFactura) {
        this.idFactura = idFactura;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Date getFechaFacturacion() {
        return fechaFacturacion;
    }

    public void setFechaFacturacion(Date fechaFacturacion) {
        this.fechaFacturacion = fechaFacturacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<Pago> getPagoList() {
        return pagoList;
    }

    public void setPagoList(List<Pago> pagoList) {
        this.pagoList = pagoList;
    }

    public List<FacturaOrdenProducto> getFacturaOrdenProductoList() {
        return facturaOrdenProductoList;
    }

    public void setFacturaOrdenProductoList(List<FacturaOrdenProducto> facturaOrdenProductoList) {
        this.facturaOrdenProductoList = facturaOrdenProductoList;
    }

    public Caja getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(Caja idCaja) {
        this.idCaja = idCaja;
    }

    public EmpleadoRol getIdEmpleadoRol() {
        return idEmpleadoRol;
    }

    public void setIdEmpleadoRol(EmpleadoRol idEmpleadoRol) {
        this.idEmpleadoRol = idEmpleadoRol;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idFactura != null ? idFactura.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Factura)) {
            return false;
        }
        Factura other = (Factura) object;
        if ((this.idFactura == null && other.idFactura != null) || (this.idFactura != null && !this.idFactura.equals(other.idFactura))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.ingenieria.pos.ppi115_2026.cafefe.entity.Factura[ idFactura=" + idFactura + " ]";
    }
    
}
