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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

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

    /**
     * ¿El producto ya tiene asignado ese tipo? Se excluye el propio registro
     * (por si se está modificando) para no contarse a sí mismo.
     */
    public boolean existeAsignacion(Producto producto, TipoProducto tipo, UUID excluir) {
        if (producto == null || producto.getIdProducto() == null || tipo == null || tipo.getIdTipoProducto() == null) {
            return false;
        }
        UUID idExcluido = excluir != null ? excluir : new UUID(0L, 0L);
        return ejecutar("No se pudo verificar si el tipo ya está asignado", ()
                -> em.createNamedQuery("ProductoTipoProducto.countByProductoYTipo", Long.class)
                        .setParameter("producto", producto)
                        .setParameter("tipo", tipo)
                        .setParameter("excluir", idExcluido)
                        .getSingleResult() > 0
        );
    }
}
