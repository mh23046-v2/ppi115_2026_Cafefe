package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

/**
 *
 * @author hernandez
 */
@Stateless
public class OrdenProductoDAO extends AbstractDataAccess<OrdenProducto> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public OrdenProductoDAO() {
        super(OrdenProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<OrdenProducto> findByIdOrden(UUID idOrden, int first, int max) {
        if (idOrden == null) {
            throw new IllegalArgumentException("El id de la orden no puede ser nulo");
        }
        return em.createNamedQuery("OrdenProducto.findByIdOrden", OrdenProducto.class)
                .setParameter("idOrden", idOrden)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }

    //Todos los productos de la orden (sin paginar), para aplicar descuentos
    public List<OrdenProducto> findByIdOrden(UUID idOrden) {
        return findByIdOrden(idOrden, 0, Integer.MAX_VALUE);
    }

    public long countByIdOrden(UUID idOrden) {
        if (idOrden == null) {
            throw new IllegalArgumentException("El id de la orden no puede ser nulo");
        }
        return em.createNamedQuery("OrdenProducto.countByIdOrden", Long.class)
                .setParameter("idOrden", idOrden)
                .getSingleResult();
    }

    //Total a pagar de la orden (da 0 si no tiene productos)
    public BigDecimal totalByIdOrden(UUID idOrden) {
        if (idOrden == null) {
            throw new IllegalArgumentException("El id de la orden no puede ser nulo");
        }
        BigDecimal total = em.createNamedQuery("OrdenProducto.sumPrecioByIdOrden", BigDecimal.class)
                .setParameter("idOrden", idOrden)
                .getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }

    public List<Producto> findProductosActivos() {
        return em.createQuery(
                "SELECT p FROM Producto p WHERE p.activo = true ORDER BY p.nombre", Producto.class)
                .getResultList();
    }

    // Descuentos asignados a los productos que ya están en la orden. La vigensia se revisa en ReglasOrden. 
    public List<DescuentoProducto> findDescuentosDeProductosEnOrden(UUID idOrden) {
        if (idOrden == null) {
            throw new IllegalArgumentException("El id de la orden no puede ser nulo");
        }
        return em.createQuery(
                "SELECT dp FROM DescuentoProducto dp JOIN dp.idDescuento d"
                + " WHERE dp.idProducto IN (SELECT op.idProducto FROM OrdenProducto op WHERE op.idOrden.idOrden = :idOrden)"
                + " ORDER BY d.nombre", DescuentoProducto.class)
                .setParameter("idOrden", idOrden)
                .getResultList();
    }
}
