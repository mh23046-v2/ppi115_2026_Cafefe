package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author johnyv
 */
@Stateless
public class TipoCaracteristicaDAO extends AbstractDataAccess<TipoCaracteristica> {
    
    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;
    
    public TipoCaracteristicaDAO() {
        super(TipoCaracteristica.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<TipoCaracteristica> findByActivo(Boolean activo) {
        return em.createNamedQuery("TipoCaracteristica.findByActivo", TipoCaracteristica.class)
                .setParameter("activo", activo)
                .getResultList();
    }
    
    public List<TipoCaracteristica> findByNombre(String nombre) {
        return em.createNamedQuery("TipoCaracteristica.findByNombre", TipoCaracteristica.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }
}
