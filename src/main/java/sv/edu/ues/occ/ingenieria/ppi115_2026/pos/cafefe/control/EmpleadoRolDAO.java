package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 *
 * @author hernandez
 */
@Stateless
public class EmpleadoRolDAO extends AbstractDataAccess<EmpleadoRol>{

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;
    
    public EmpleadoRolDAO() {
        super(EmpleadoRol.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
     public List<EmpleadoRol> findByActivo(Boolean activo) {
        return em.createNamedQuery("EmpleadoRol.findByActivo", EmpleadoRol.class)
                .setParameter("activo", activo)
                .getResultList();
    }

    public List<EmpleadoRol> findByRol(Rol rol) {
        return em.createQuery(
                "SELECT e FROM EmpleadoRol e WHERE e.idRol = :rol",
                EmpleadoRol.class)
                .setParameter("rol", rol)
                .getResultList();
    }

    public List<EmpleadoRol> findByEmpleado(Empleado empleado) {
        return em.createQuery(
                "SELECT e FROM EmpleadoRol e WHERE e.idEmpleado = :empleado",
                EmpleadoRol.class)
                .setParameter("empleado", empleado)
                .getResultList();
    }

    public List<EmpleadoRol> findByIdEmpleado(UUID idEmpleado, int first, int max) {
        if (idEmpleado == null) {
            throw new IllegalArgumentException("El id del empleado no puede ser nulo");
        }
        return em.createNamedQuery("EmpleadoRol.findByIdEmpleado", EmpleadoRol.class)
                .setParameter("idEmpleado", idEmpleado)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }

    public long countByIdEmpleado(UUID idEmpleado) {
        if (idEmpleado == null) {
            throw new IllegalArgumentException("El id del empleado no puede ser nulo");
        }
        return em.createNamedQuery("EmpleadoRol.countByIdEmpleado", Long.class)
                .setParameter("idEmpleado", idEmpleado)
                .getSingleResult();
    }
    
}
