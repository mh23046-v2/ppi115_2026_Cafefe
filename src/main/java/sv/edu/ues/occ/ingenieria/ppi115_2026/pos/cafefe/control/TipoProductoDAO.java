package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

/**
 *
 * @author hernandez
 */
@Stateless
public class TipoProductoDAO extends AbstractDataAccess<TipoProducto> {

    @PersistenceContext(unitName="CafefePU")
    private EntityManager em;
    
    public TipoProductoDAO() {
        super(TipoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public List<TipoProducto> findByActivo(boolean activo){
        return em.createNamedQuery("TipoProducto.findByActivo", TipoProducto.class)
                .setParameter("activo", activo).getResultList();   
    }
    
    public List<TipoProducto> findByNombre(String nombre) {
        return em.createNamedQuery("TipoProducto.findByNombre", TipoProducto.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }
    
}
