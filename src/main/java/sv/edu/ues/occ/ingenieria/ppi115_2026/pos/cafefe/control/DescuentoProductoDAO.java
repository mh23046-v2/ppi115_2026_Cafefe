package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

/**
 *
 * @author johnyv
 */
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

    public List<DescuentoProducto> findByProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            return List.of();
        }
        return em.createQuery("SELECT dp FROM DescuentoProducto dp WHERE dp.idProducto = :producto", DescuentoProducto.class)
                .setParameter("producto", producto)
                .getResultList();
    }

    public List<DescuentoProducto> findByFechaDesde(Date fechaDesde) {
        return em.createNamedQuery("DescuentoProducto.findByFechaDesde", DescuentoProducto.class)
                .setParameter("fechaDesde", fechaDesde)
                .getResultList();
    }

    public List<DescuentoProducto> findByFechaHasta(Date fechaHasta) {
        return em.createNamedQuery("DescuentoProducto.findByFechaHasta", DescuentoProducto.class)
                .setParameter("fechaHasta", fechaHasta)
                .getResultList();
    }

    public List<DescuentoProducto> findByValor(Integer valor) {
        return em.createNamedQuery("DescuentoProducto.findByValor", DescuentoProducto.class)
                .setParameter("valor", valor)
                .getResultList();
    }
}