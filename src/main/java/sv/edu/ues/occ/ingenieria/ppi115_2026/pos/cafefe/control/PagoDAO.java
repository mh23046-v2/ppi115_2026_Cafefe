package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;

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
}