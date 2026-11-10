package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;

@Stateless
public class FacturaOrdenProductoDAO extends AbstractDataAccess<FacturaOrdenProducto> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public FacturaOrdenProductoDAO() {
        super(FacturaOrdenProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<FacturaOrdenProducto> findByFactura(Factura factura, int first, int max) {
        if (factura == null || factura.getIdFactura() == null) {
            throw new IllegalArgumentException("La factura es obligatoria");
        }
        validarRango(first, max);

        return ejecutar("No se pudo cargar el detalle de ordenes de la factura", () ->
            em.createNamedQuery("FacturaOrdenProducto.findByFactura", FacturaOrdenProducto.class)
                .setParameter("factura", factura)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByFactura(Factura factura) {
        if (factura == null || factura.getIdFactura() == null) {
            throw new IllegalArgumentException("La factura es obligatoria");
        }

        return ejecutar("No se pudo contar el detalle de ordenes de la factura", () -> {
            Long total = em.createNamedQuery("FacturaOrdenProducto.countByFactura", Long.class)
                .setParameter("factura", factura)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    public List<FacturaOrdenProducto> findByOrdenProducto(OrdenProducto ordenProducto, int first, int max) {
        if (ordenProducto == null || ordenProducto.getIdOrdenProducto() == null) {
            throw new IllegalArgumentException("El orden producto es obligatorio");
        }
        validarRango(first, max);

        return ejecutar("No se pudo cargar la relación por orden producto", () ->
            em.createNamedQuery("FacturaOrdenProducto.findByOrdenProducto", FacturaOrdenProducto.class)
                .setParameter("ordenProducto", ordenProducto)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByOrdenProducto(OrdenProducto ordenProducto) {
        if (ordenProducto == null || ordenProducto.getIdOrdenProducto() == null) {
            throw new IllegalArgumentException("El orden producto es obligatorio");
        }

        return ejecutar("No se pudo contar la relación por orden producto", () -> {
            Long total = em.createNamedQuery("FacturaOrdenProducto.countByOrdenProducto", Long.class)
                .setParameter("ordenProducto", ordenProducto)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<FacturaOrdenProducto> root) {
        return List.of(cb.asc(root.get("idFacturaOrdenProducto")));
    }

    /** Suma de los precios de la factura (0 si no tiene productos). */
    public BigDecimal totalByFactura(Factura factura) {
        if (factura == null || factura.getIdFactura() == null) {
            return BigDecimal.ZERO;
        }
        return ejecutar("No se pudo calcular el total de la factura", () -> {
            BigDecimal total = em.createNamedQuery("FacturaOrdenProducto.sumPrecioByFactura", BigDecimal.class)
                .setParameter("factura", factura)
                .getSingleResult();
            return total == null ? BigDecimal.ZERO : total;
        });
    }

    /** ¿En cuántas facturas no anuladas está ya este producto de orden? (debe ser 0 para facturarlo). */
    public int countVigentesByOrdenProducto(OrdenProducto ordenProducto) {
        if (ordenProducto == null || ordenProducto.getIdOrdenProducto() == null) {
            throw new IllegalArgumentException("El producto de la orden es obligatorio");
        }
        return ejecutar("No se pudo verificar si el producto ya fue facturado", () -> {
            Long total = em.createNamedQuery("FacturaOrdenProducto.countVigentesByOrdenProducto", Long.class)
                .setParameter("ordenProducto", ordenProducto)
                .setParameter("anulada", ReglasFactura.ANULADA)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    /**
     * Agrega a la factura todos los productos de la orden que aún no están
     * facturados, copiando su precio. Al ser un método de un @Stateless, todo
     * corre en una sola transacción: se agregan todos o ninguno.
     *
     * @return cuántos productos se agregaron
     */
    public int agregarPendientesDeOrden(Factura factura, Orden orden) {
        if (factura == null || factura.getIdFactura() == null) {
            throw new IllegalArgumentException("Guarde primero la factura");
        }
        if (orden == null || orden.getIdOrden() == null) {
            throw new IllegalArgumentException("Seleccione una orden");
        }
        return ejecutar("No se pudieron agregar los productos de la orden", () -> {
            List<OrdenProducto> pendientes = em.createNamedQuery("OrdenProducto.findPendientesByOrden", OrdenProducto.class)
                .setParameter("orden", orden)
                .setParameter("anulada", ReglasFactura.ANULADA)
                .getResultList();
            Factura destino = em.merge(factura);
            for (OrdenProducto op : pendientes) {
                FacturaOrdenProducto fop = new FacturaOrdenProducto(UUID.randomUUID());
                fop.setIdFactura(destino);
                fop.setIdOrdenProducto(op);
                fop.setPrecio(op.getPrecio());
                fop.setObservaciones(op.getObservaciones());
                em.persist(fop);
            }
            em.flush();
            return pendientes.size();
        });
    }
}
