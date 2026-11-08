package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;

@Stateless
public class ProductoTipoProductoDAO extends AbstractDataAccess<ProductoTipoProducto> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public ProductoTipoProductoDAO() {
        super(ProductoTipoProducto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<ProductoTipoProducto> root) {
        return List.of(
                cb.desc(root.get("fechaCreacion")),
                cb.asc(root.get("idProductoTipoProducto"))
        );
    }

    public List<ProductoTipoProducto> findByProducto(Producto producto, int first, int max) {
        if (producto == null || producto.getIdProducto() == null) {
            return Collections.emptyList();
        }
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        return ejecutar("No se pudieron buscar los tipos del producto", ()
                -> em.createNamedQuery("ProductoTipoProducto.findByProducto", ProductoTipoProducto.class)
                        .setParameter("producto", producto)
                        .setFirstResult(first)
                        .setMaxResults(max)
                        .getResultList()
        );
    }

    public Long countByProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return 0L;
        }
        return ejecutar("No se pudo contar los tipos del producto", ()
                -> em.createNamedQuery("ProductoTipoProducto.countByProducto", Long.class)
                        .setParameter("producto", producto)
                        .getSingleResult()
        );
    }
}
