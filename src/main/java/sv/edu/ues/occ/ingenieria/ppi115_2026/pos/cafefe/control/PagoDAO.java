package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;

@Stateless
public class PagoDAO extends AbstractDataAccess<Pago> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public PagoDAO() {
        super(Pago.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<Pago> findByEstado(String estado, int first, int max) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        validarRango(first, max);

        return ejecutar("No se pudieron cargar los pagos por estado", () ->
            em.createNamedQuery("Pago.findByEstado", Pago.class)
                .setParameter("estado", estado)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }

        return ejecutar("No se pudo contar los pagos por estado", () -> {
            Long total = em.createNamedQuery("Pago.countByEstado", Long.class)
                .setParameter("estado", estado)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    public List<Pago> findByFactura(Factura factura, int first, int max) {
        if (factura == null || factura.getIdFactura() == null) {
            throw new IllegalArgumentException("La factura es obligatoria");
        }
        validarRango(first, max);

        return ejecutar("No se pudieron cargar los pagos de la factura", () ->
            em.createNamedQuery("Pago.findByFactura", Pago.class)
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

        return ejecutar("No se pudo contar los pagos de la factura", () -> {
            Long total = em.createNamedQuery("Pago.countByFactura", Long.class)
                .setParameter("factura", factura)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<Pago> root) {
        return List.of(cb.desc(root.get("idFactura").get("fechaFacturacion")), cb.asc(root.get("idPago")));
    }

    /** Listado de la pantalla: los más recientes primero, con su factura ya cargada. */
    @Override
    public List<Pago> findRange(int first, int max) {
        validarRango(first, max);
        return ejecutar("No se pudieron cargar los pagos", () ->
            em.createNamedQuery("Pago.findRecientes", Pago.class)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    /**
     * Pagos de la factura que no están anulados, sin contar el propio pago.
     * Una factura solo puede tener un pago vigente a la vez.
     */
    public int countVigentesByFactura(Factura factura, UUID excluir) {
        if (factura == null || factura.getIdFactura() == null) {
            return 0;
        }
        UUID idExcluido = excluir != null ? excluir : new UUID(0L, 0L);
        return ejecutar("No se pudo verificar los pagos de la factura", () -> {
            Long total = em.createNamedQuery("Pago.countVigentesByFactura", Long.class)
                .setParameter("factura", factura)
                .setParameter("anulado", ReglasPago.ANULADO)
                .setParameter("excluir", idExcluido)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    /**
     * Completa el pago: valida lo pagado contra el total de la factura y deja
     * el pago COMPLETADO y la factura PAGADA. Todo en UNA transacción (método de
     * un EJB @Stateless): si una regla falla se lanza ReglaNegocioException y
     * el contenedor revierte todo, así no queda un pago completado con la
     * factura pendiente ni al revés.
     *
     * @return el cambio a entregar al cliente
     */
    public BigDecimal completar(Pago pago) {
        if (pago == null || pago.getIdPago() == null) {
            throw new IllegalArgumentException("El pago es obligatorio");
        }
        return ejecutar("No se pudo completar el pago", () -> {
            Pago enBase = em.find(Pago.class, pago.getIdPago());
            if (enBase == null) {
                throw new ReglaNegocioException("Guarde primero el pago");
            }
            if (!ReglasPago.PENDIENTE.equals(enBase.getEstado())) {
                throw new ReglaNegocioException("El pago ya está " + enBase.getEstado());
            }
            Factura factura = enBase.getIdFactura();
            if (factura == null || !ReglasFactura.PENDIENTE.equals(factura.getEstado())) {
                throw new ReglaNegocioException("Solo se cobra una factura PENDIENTE");
            }
            BigDecimal total = em.createNamedQuery("FacturaOrdenProducto.sumPrecioByFactura", BigDecimal.class)
                .setParameter("factura", factura)
                .getSingleResult();
            List<PagoDetalle> detalles = em.createNamedQuery("PagoDetalle.findByPagoOrdenado", PagoDetalle.class)
                .setParameter("pago", enBase)
                .getResultList();
            String error = ReglasPago.validarCierre(total, detalles);
            if (error != null) {
                throw new ReglaNegocioException(error);
            }
            // Ambas entidades están administradas: los cambios se guardan al confirmar la transacción.
            enBase.setEstado(ReglasPago.COMPLETADO);
            factura.setEstado(ReglasFactura.PAGADA);
            em.flush();
            pago.setEstado(ReglasPago.COMPLETADO);
            return ReglasPago.cambio(total, detalles);
        });
    }

    /**
     * Anula el pago. Si estaba COMPLETADO, la factura vuelve a PENDIENTE para
     * poder cobrarla de nuevo (o anularla). Una sola transacción.
     */
    public void anular(Pago pago) {
        if (pago == null || pago.getIdPago() == null) {
            throw new IllegalArgumentException("El pago es obligatorio");
        }
        ejecutar("No se pudo anular el pago", () -> {
            Pago enBase = em.find(Pago.class, pago.getIdPago());
            if (enBase == null) {
                throw new ReglaNegocioException("El pago no existe");
            }
            if (ReglasPago.ANULADO.equals(enBase.getEstado())) {
                throw new ReglaNegocioException("El pago ya está ANULADO");
            }
            Factura factura = enBase.getIdFactura();
            if (ReglasPago.COMPLETADO.equals(enBase.getEstado()) && factura != null
                    && ReglasFactura.PAGADA.equals(factura.getEstado())) {
                factura.setEstado(ReglasFactura.PENDIENTE);
            }
            enBase.setEstado(ReglasPago.ANULADO);
            em.flush();
            pago.setEstado(ReglasPago.ANULADO);
            return null;
        });
    }

    /** Elimina el pago junto con sus detalles, en una sola transacción. */
    @Override
    public void delete(Pago pago) {
        if (pago == null || pago.getIdPago() == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo ni estar sin ID");
        }
        ejecutar("No se pudo eliminar el detalle del pago", () -> {
            em.createNamedQuery("PagoDetalle.deleteByPago")
                .setParameter("idPago", pago.getIdPago())
                .executeUpdate();
            return null;
        });
        super.delete(pago);
    }
}
