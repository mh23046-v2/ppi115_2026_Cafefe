package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@Stateless
public class DescuentoProductoDAO extends AbstractDataAccess<DescuentoProducto> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public DescuentoProductoDAO() {
        super(DescuentoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<DescuentoProducto> findByProducto(Producto producto, int first, int max) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Los parámetros de paginación son inválidos");
        }
        return ejecutar("No se pudieron cargar los descuentos del producto", () ->
            em.createNamedQuery("DescuentoProducto.findByProducto", DescuentoProducto.class)
                .setParameter("producto", producto)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        return ejecutar("No se pudo contar los descuentos del producto", () -> {
            Long total = em.createNamedQuery("DescuentoProducto.countByProducto", Long.class)
                    .setParameter("producto", producto)
                    .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }
    
    @Override
    public List<DescuentoProducto> findRange(int first, int max) {
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Los parámetros de paginación son inválidos");
        }
        return ejecutar("No se pudieron cargar los registros de descuento_producto", () ->
            em.createNamedQuery("DescuentoProducto.findConRelaciones", DescuentoProducto.class)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }
}