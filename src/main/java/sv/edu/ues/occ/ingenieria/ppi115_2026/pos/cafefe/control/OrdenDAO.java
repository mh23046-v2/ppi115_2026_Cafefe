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
    /**
     * Condición "la orden tiene al menos un producto sin cobrar": el producto no
     * está en ninguna factura, o solo en facturas ANULADAS. Es la misma que usa
     * la named query Orden.findConPendientesDeFacturar.
     */
    static final String CON_PENDIENTES = "EXISTS (SELECT op FROM OrdenProducto op WHERE op.idOrden = o"
            + " AND NOT EXISTS (SELECT f FROM FacturaOrdenProducto f WHERE f.idOrdenProducto = op"
            + " AND (f.idFactura.estado IS NULL OR f.idFactura.estado <> :anulada)))";

    static final String JPQL_PENDIENTES = "SELECT o FROM Orden o WHERE " + CON_PENDIENTES
            + " ORDER BY o.fechaCreacion ASC, o.idOrden";

    static final String JPQL_COUNT_PENDIENTES = "SELECT COUNT(o) FROM Orden o WHERE " + CON_PENDIENTES;

    /**
     * Órdenes pendientes de cobro, paginadas, de la más antigua a la más
     * reciente (la cocina atiende primero la que lleva más tiempo esperando).
     * Las usan la pantalla de Cocina (HU3) y la de Caja (HU4).
     */
    public List<Orden> findPendientesDeCobro(int first, int max) {
        validarRango(first, max);
        return ejecutar("No se pudieron consultar las órdenes pendientes", () ->
            em.createQuery(JPQL_PENDIENTES, Orden.class)
                .setParameter("anulada", ReglasFactura.ANULADA)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    /** Cuántas órdenes tienen productos sin cobrar (para el header Total-registros). */
    public int countPendientesDeCobro() {
        return ejecutar("No se pudieron contar las órdenes pendientes", () -> {
            Long total = em.createQuery(JPQL_COUNT_PENDIENTES, Long.class)
                .setParameter("anulada", ReglasFactura.ANULADA)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }
}
