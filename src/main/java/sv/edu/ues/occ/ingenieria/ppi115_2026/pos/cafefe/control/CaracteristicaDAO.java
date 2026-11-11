package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author johnyv
 */
@Stateless
public class CaracteristicaDAO extends AbstractDataAccess<Caracteristica> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public CaracteristicaDAO() {
        super(Caracteristica.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<Caracteristica> root) {
        return List.of(
            cb.asc(root.get("nombre")), 
            cb.asc(root.get("idCaracteristica"))
        );
    }
    
    public List<Caracteristica> buscarActivasPorTipoYNombre(TipoCaracteristica tipo, String texto, int max) {
        validarTipo(tipo);
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar las características del tipo", () ->
            em.createNamedQuery("Caracteristica.buscarActivasPorTipoYNombre", Caracteristica.class)
                .setParameter("tipo", tipo)
                .setParameter("texto", patronLike(texto))
                .setMaxResults(max)
                .getResultList()
        );
    }

    public List<Caracteristica> findByTipo(TipoCaracteristica tipo, int first, int max) {
        validarTipo(tipo);
        validarRango(first, max);
        return ejecutar("No se pudieron cargar las características del tipo", () ->
            em.createNamedQuery("Caracteristica.findByTipo", Caracteristica.class)
                .setParameter("tipo", tipo)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public long countByTipo(TipoCaracteristica tipo) {
        validarTipo(tipo);
        return ejecutar("No se pudo contar las características del tipo", () ->
            em.createNamedQuery("Caracteristica.countByTipo", Long.class)
                .setParameter("tipo", tipo)
                .getSingleResult()
        );
    }

    private static void validarTipo(TipoCaracteristica tipo) {
        if (tipo == null || tipo.getIdTipoCaracteristica() == null) {
            throw new IllegalArgumentException("El tipo de característica es obligatorio");
        }
    }
}