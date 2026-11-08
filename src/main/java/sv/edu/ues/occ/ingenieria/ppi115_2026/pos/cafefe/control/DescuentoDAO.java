package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@Stateless
public class DescuentoDAO extends AbstractDataAccess<Descuento> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public DescuentoDAO() {
        super(Descuento.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<Descuento> root) {
        return List.of(
            cb.asc(root.get("nombre")),
            cb.asc(root.get("idDescuento"))
        );
    }

    public List<Descuento> buscarActivosPorTipoYNombre(TipoDescuento tipo, String texto, int max) {
        if (tipo == null || tipo.getIdTipoDescuento() == null) {
            throw new IllegalArgumentException("El tipo de descuento es obligatorio");
        }
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar los descuentos activos por tipo", () ->
            em.createNamedQuery("Descuento.buscarActivosPorTipoYNombre", Descuento.class)
                .setParameter("tipo", tipo)
                .setParameter("texto", patronLike(texto))
                .setMaxResults(max)
                .getResultList()
        );
    }
}