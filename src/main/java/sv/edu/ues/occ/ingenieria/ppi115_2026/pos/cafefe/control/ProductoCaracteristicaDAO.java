package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;

/**
 *
 * @author johnyv
 */
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

    public List<ProductoCaracteristica> findByProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return List.of();
        }
        return em.createQuery("SELECT pc FROM ProductoCaracteristica pc WHERE pc.idProducto = :producto", ProductoCaracteristica.class)
                .setParameter("producto", producto)
                .getResultList();
    }

    public List<ProductoCaracteristica> findByValor(String valor) {
        return em.createNamedQuery("ProductoCaracteristica.findByValor", ProductoCaracteristica.class)
                .setParameter("valor", valor)
                .getResultList();
    }
}