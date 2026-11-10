package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

@Stateless
public class FacturaDAO extends AbstractDataAccess<Factura> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public FacturaDAO() {
        super(Factura.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<Factura> findByEstado(String estado, int first, int max) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        validarRango(first, max);

        return ejecutar("No se pudieron cargar las facturas por estado", () ->
            em.createNamedQuery("Factura.findByEstado", Factura.class)
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

        return ejecutar("No se pudo contar las facturas por estado", () -> {
            Long total = em.createNamedQuery("Factura.countByEstado", Long.class)
                .setParameter("estado", estado)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    public List<Factura> findByCaja(Caja caja, int first, int max) {
        if (caja == null || caja.getIdCaja() == null) {
            throw new IllegalArgumentException("La caja es obligatoria");
        }
        validarRango(first, max);

        return ejecutar("No se pudieron cargar las facturas por caja", () ->
            em.createNamedQuery("Factura.findByCaja", Factura.class)
                .setParameter("caja", caja)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByCaja(Caja caja) {
        if (caja == null || caja.getIdCaja() == null) {
            throw new IllegalArgumentException("La caja es obligatoria");
        }

        return ejecutar("No se pudo contar las facturas por caja", () -> {
            Long total = em.createNamedQuery("Factura.countByCaja", Long.class)
                .setParameter("caja", caja)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    public List<Factura> findByCliente(String cliente, int first, int max) {
        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        validarRango(first, max);

        return ejecutar("No se pudieron cargar las facturas por cliente", () ->
            em.createNamedQuery("Factura.findByCliente", Factura.class)
                .setParameter("cliente", patronLike(cliente))
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByCliente(String cliente) {
        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }

        return ejecutar("No se pudo contar las facturas por cliente", () -> {
            Long total = em.createNamedQuery("Factura.countByCliente", Long.class)
                .setParameter("cliente", patronLike(cliente))
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    // Más recientes primero; el id desempata para que la paginación sea estable.
    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<Factura> root) {
        return List.of(cb.desc(root.get("fechaFacturacion")), cb.asc(root.get("idFactura")));
    }

    @Override
    public List<Factura> findRange(int first, int max) {
        validarRango(first, max);
        return ejecutar("No se pudieron cargar las facturas", () ->
            em.createNamedQuery("Factura.findRecientes", Factura.class)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    // Elimina la factura junto con sus productos en la misma transacción (igual que OrdenDAO).
    @Override
    public void delete(Factura factura) {
        if (factura == null || factura.getIdFactura() == null) {
            throw new IllegalArgumentException("La factura no puede ser nula ni estar sin ID");
        }
        ejecutar("No se pudo eliminar el detalle de la factura", () -> {
            em.createNamedQuery("FacturaOrdenProducto.deleteByFactura")
                .setParameter("idFactura", factura.getIdFactura())
                .executeUpdate();
            return null;
        });
        super.delete(factura);
    }
}
