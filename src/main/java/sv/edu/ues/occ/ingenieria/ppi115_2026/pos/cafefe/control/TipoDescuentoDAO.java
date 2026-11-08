package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@Stateless
public class TipoDescuentoDAO extends AbstractDataAccess<TipoDescuento> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public TipoDescuentoDAO() {
        super(TipoDescuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<TipoDescuento> root) {
        return List.of(
            cb.asc(root.get("nombre")),
            cb.asc(root.get("idTipoDescuento"))
        );
    }

    public List<TipoDescuento> buscarPorNombre(String texto, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar los tipos de descuento", () ->
            em.createNamedQuery("TipoDescuento.buscarPorNombre", TipoDescuento.class)
                .setParameter("texto", patronLike(texto))
                .setMaxResults(max)
                .getResultList()
        );
    }
}