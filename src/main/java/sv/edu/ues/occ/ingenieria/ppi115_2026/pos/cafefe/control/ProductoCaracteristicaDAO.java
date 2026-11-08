package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;

@Stateless
public class ProductoCaracteristicaDAO extends AbstractDataAccess<ProductoCaracteristica> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public ProductoCaracteristicaDAO() {
        super(ProductoCaracteristica.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<ProductoCaracteristica> root) {
        return List.of(
            cb.asc(root.get("idProductoCaracteristica"))
        );
    }

    // Recupera una página de características asignadas a un producto.
    public List<ProductoCaracteristica> findByProducto(Producto producto, int first, int max) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Los parámetros de paginación son inválidos");
        }
        return ejecutar("No se pudieron cargar las características del producto", () ->
            em.createNamedQuery("ProductoCaracteristica.findByProducto", ProductoCaracteristica.class)
                .setParameter("producto", producto)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    // Cuenta el total de características asignadas a un producto para el paginador. 
    public int countByProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        return ejecutar("No se pudo contar las características del producto", () -> {
            Long total = em.createNamedQuery("ProductoCaracteristica.countByProducto", Long.class)
                    .setParameter("producto", producto)
                    .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }
}