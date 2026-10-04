package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;

/**
 *
 * @author johnyv
 */
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

    public List<ProductoTipoProducto> findByProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return List.of();
        }
        return em.createQuery("SELECT ptp FROM ProductoTipoProducto ptp WHERE ptp.producto = :producto", ProductoTipoProducto.class)
                .setParameter("producto", producto)
                .getResultList();
    }

    public List<ProductoTipoProducto> findByFechaCreacion(Date fechaCreacion) {
        return em.createNamedQuery("ProductoTipoProducto.findByFechaCreacion", ProductoTipoProducto.class)
                .setParameter("fechaCreacion", fechaCreacion)
                .getResultList();
    }

    public List<ProductoTipoProducto> findByIdProducto(UUID idProducto) {
        if (idProducto == null) {
            return Collections.emptyList();
        }
        return em.createQuery(
                "SELECT ptp FROM ProductoTipoProducto ptp WHERE ptp.producto.idProducto = :idProducto",
                ProductoTipoProducto.class)
                .setParameter("idProducto", idProducto)
                .getResultList();
    }
}
