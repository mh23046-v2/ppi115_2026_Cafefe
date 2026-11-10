package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.math.BigDecimal;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;

@Stateless
public class PagoDetalleDAO extends AbstractDataAccess<PagoDetalle> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public PagoDetalleDAO() {
        super(PagoDetalle.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<PagoDetalle> findByPago(Pago pago, int first, int max) {
        if (pago == null || pago.getIdPago() == null) {
            throw new IllegalArgumentException("El pago es obligatorio");
        }
        validarRango(first, max);

        return ejecutar("No se pudo cargar el detalle del pago", () ->
            em.createNamedQuery("PagoDetalle.findByPago", PagoDetalle.class)
                .setParameter("pago", pago)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByPago(Pago pago) {
        if (pago == null || pago.getIdPago() == null) {
            throw new IllegalArgumentException("El pago es obligatorio");
        }

        return ejecutar("No se pudo contar el detalle del pago", () -> {
            Long total = em.createNamedQuery("PagoDetalle.countByPago", Long.class)
                .setParameter("pago", pago)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    public List<PagoDetalle> findByTipoPago(String tipoPago, int first, int max) {
        if (tipoPago == null || tipoPago.isBlank()) {
            throw new IllegalArgumentException("El tipo de pago es obligatorio");
        }
        validarRango(first, max);

        return ejecutar("No se pudieron cargar los detalles por tipo de pago", () ->
            em.createNamedQuery("PagoDetalle.findByTipoPago", PagoDetalle.class)
                .setParameter("tipoPago", tipoPago)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByTipoPago(String tipoPago) {
        if (tipoPago == null || tipoPago.isBlank()) {
            throw new IllegalArgumentException("El tipo de pago es obligatorio");
        }

        return ejecutar("No se pudo contar los detalles por tipo de pago", () -> {
            Long total = em.createNamedQuery("PagoDetalle.countByTipoPago", Long.class)
                .setParameter("tipoPago", tipoPago)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<PagoDetalle> root) {
        return List.of(cb.asc(root.get("tipoPago")), cb.asc(root.get("idPagoDetalle")));
    }

    /** Detalles del pago, paginados y en orden estable (para la pestaña de detalles). */
    public List<PagoDetalle> findByPagoOrdenado(Pago pago, int first, int max) {
        if (pago == null || pago.getIdPago() == null) {
            throw new IllegalArgumentException("El pago es obligatorio");
        }
        validarRango(first, max);
        return ejecutar("No se pudo cargar el detalle del pago", () ->
            em.createNamedQuery("PagoDetalle.findByPagoOrdenado", PagoDetalle.class)
                .setParameter("pago", pago)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    /** Suma de los montos del pago (0 si no tiene detalles). */
    public BigDecimal totalByPago(Pago pago) {
        if (pago == null || pago.getIdPago() == null) {
            return BigDecimal.ZERO;
        }
        return ejecutar("No se pudo calcular lo pagado", () -> {
            BigDecimal total = em.createNamedQuery("PagoDetalle.sumMontoByPago", BigDecimal.class)
                .setParameter("pago", pago)
                .getSingleResult();
            return total != null ? total : BigDecimal.ZERO;
        });
    }
}
