package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;

/**
 *
 * @author hernandez
 */
@Stateless
public class EmpleadoDAO extends AbstractDataAccess<Empleado>{
    
    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;
    
    public EmpleadoDAO() {
        super(Empleado.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<Empleado> findByActivo(Boolean activo) {
        return em.createNamedQuery("Empleado.findByActivo", Empleado.class)
                .setParameter("activo", activo)
                .getResultList();
    }

    public List<Empleado> findByNombre(String nombre) {
        return em.createNamedQuery("Empleado.findByNombre", Empleado.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }

    public List<Empleado> findByApellido(String apellido) {
        return em.createNamedQuery("Empleado.findByApellido", Empleado.class)
                .setParameter("apellido", apellido)
                .getResultList();
    }
    
}
