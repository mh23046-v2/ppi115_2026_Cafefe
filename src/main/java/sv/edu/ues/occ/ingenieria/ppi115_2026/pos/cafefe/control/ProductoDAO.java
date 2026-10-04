package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

/**
 *
 * @author johnyv
 */
@Stateless
public class ProductoDAO extends AbstractDataAccess<Producto> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public ProductoDAO() {
        super(Producto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<Producto> findByActivo(Boolean activo) {
        return em.createNamedQuery("Producto.findByActivo", Producto.class)
                .setParameter("activo", activo)
                .getResultList();
    }

    public List<Producto> findByNombre(String nombre) {
        return em.createNamedQuery("Producto.findByNombre", Producto.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }
}