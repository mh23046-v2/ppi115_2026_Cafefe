package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;

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
}