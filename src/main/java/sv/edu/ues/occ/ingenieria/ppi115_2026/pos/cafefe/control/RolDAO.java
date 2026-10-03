package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 *
 * @author hernandez
 */
@Stateless
public class RolDAO extends AbstractDataAccess<Rol>{
    
    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;
    
    public RolDAO() {
        super(Rol.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
     public List<Rol> findByActivo(Boolean activo) {
        return em.createNamedQuery("Rol.findByActivo", Rol.class)
                .setParameter("activo", activo)
                .getResultList();
    }

    public List<Rol> findByNombre(String nombre) {
        return em.createNamedQuery("Rol.findByNombre", Rol.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }
    
}
