package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

/**
 *
 * @author johnyv
 */
@Stateless
public class TipoDescuentoDAO extends AbstractDataAccess<TipoDescuento> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public TipoDescuentoDAO() {
        super(TipoDescuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<TipoDescuento> findByActivo(Boolean activo) {
        return em.createNamedQuery("TipoDescuento.findByActivo", TipoDescuento.class)
                .setParameter("activo", activo)
                .getResultList();
    }

    public List<TipoDescuento> findByNombre(String nombre) {
        return em.createNamedQuery("TipoDescuento.findByNombre", TipoDescuento.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }
}
