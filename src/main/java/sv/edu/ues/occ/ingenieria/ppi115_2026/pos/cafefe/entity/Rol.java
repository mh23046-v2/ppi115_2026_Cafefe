package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.json.bind.annotation.JsonbTransient;
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

@Entity
@Table(name = "rol")
@NamedQueries({
    @NamedQuery(name = "Rol.buscarActivosPorNombre", query = "SELECT r FROM Rol r WHERE r.activo = true AND LOWER(r.nombre) LIKE :nombre ORDER BY r.nombre, r.idRol"),
    @NamedQuery(name = "Rol.findActivos", query = "SELECT r FROM Rol r WHERE r.activo = true ORDER BY r.nombre, r.idRol"),
    @NamedQuery(name = "Rol.findAll", query = "SELECT r FROM Rol r"),
    @NamedQuery(name = "Rol.findByNombre", query = "SELECT r FROM Rol r WHERE LOWER(r.nombre) LIKE :nombre ORDER BY r.activo DESC, r.nombre ASC"),
    @NamedQuery(name = "Rol.countByNombre", query = "SELECT COUNT(r) FROM Rol r WHERE LOWER(r.nombre) LIKE :nombre"),
    @NamedQuery(name = "Rol.findByObservaciones", query = "SELECT r FROM Rol r WHERE r.observaciones = :observaciones")})
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Convert(converter = UUIDConverter.class)
    @Column(name = "id_rol")
    private UUID idRol;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 155)
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @JsonbTransient
    @OneToMany(mappedBy = "idRol", fetch = FetchType.LAZY)
    private List<EmpleadoRol> empleadoRolList;

    public Rol() {
    }

    public Rol(UUID idRol) {
        this.idRol = idRol;
    }

    public UUID getIdRol() {
        return idRol;
    }

    public void setIdRol(UUID idRol) {
        this.idRol = idRol;
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

    public List<EmpleadoRol> getEmpleadoRolList() {
        return empleadoRolList;
    }

    public void setEmpleadoRolList(List<EmpleadoRol> empleadoRolList) {
        this.empleadoRolList = empleadoRolList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idRol != null ? idRol.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Rol)) {
            return false;
        }
        Rol other = (Rol) object;
        if ((this.idRol == null && other.idRol != null) || (this.idRol != null && !this.idRol.equals(other.idRol))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues.ingenieria.pos.ppi115_2026.cafefe.entity.Rol[ idRol=" + idRol + " ]";
    }
    
}
