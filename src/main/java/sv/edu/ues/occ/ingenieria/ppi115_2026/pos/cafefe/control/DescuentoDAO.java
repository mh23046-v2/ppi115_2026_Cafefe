package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;

/**
 *
 * @author johnyv
 */
@Stateless
public class DescuentoDAO extends AbstractDataAccess<Descuento> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public DescuentoDAO() {
        super(Descuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<Descuento> findByActivo(Boolean activo) {
        return em.createNamedQuery("Caracteristica.findByActivo", Descuento.class)
                .setParameter("activo", activo)
                .getResultList();
    }
    
    public List<Descuento> findByNombre(String nombre) {
        return em.createNamedQuery("Descuento.findByNombre", Descuento.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }
}