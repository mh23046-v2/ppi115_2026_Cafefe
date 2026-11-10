package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;

@Stateless
public class OrdenDAO extends AbstractDataAccess<Orden> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public OrdenDAO() {
        super(Orden.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<Orden> root) {
        return List.of(
            cb.desc(root.get("fechaCreacion"))
        );
    }

    public List<Orden> findRange(int first, int max) {
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        return ejecutar("No se pudieron cargar las órdenes", () ->
            em.createNamedQuery("Orden.findAll", Orden.class)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countAll() {
        return ejecutar("No se pudo contar las órdenes", () -> {
            Long count = em.createNamedQuery("Orden.countAll", Long.class).getSingleResult();
            return count != null ? count.intValue() : 0;
        });
    }

    // Elimina la orden junto con sus productos asociados en la misma transacción.
    @Override
    public void delete(Orden orden) {
        if (orden == null || orden.getIdOrden() == null) {
            throw new IllegalArgumentException("La orden no puede ser nula ni estar sin ID");
        }
        ejecutar("No se pudo eliminar el detalle de la orden", () -> {
            em.createQuery("DELETE FROM OrdenProducto op WHERE op.idOrden.idOrden = :idOrden")
                    .setParameter("idOrden", orden.getIdOrden())
                    .executeUpdate();
            return null;
        });
        super.delete(orden);
    }

    /** Órdenes con al menos un producto sin facturar (para "Agregar orden completa"). */
    public List<Orden> findConPendientesDeFacturar(int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar las órdenes pendientes de facturar", () ->
            em.createNamedQuery("Orden.findConPendientesDeFacturar", Orden.class)
                .setParameter("anulada", ReglasFactura.ANULADA)
                .setMaxResults(max)
                .getResultList()
        );
    }
}
