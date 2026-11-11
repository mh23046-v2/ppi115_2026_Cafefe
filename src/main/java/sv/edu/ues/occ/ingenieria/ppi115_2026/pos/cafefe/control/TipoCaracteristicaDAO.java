package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@Stateless
public class TipoCaracteristicaDAO extends AbstractDataAccess<TipoCaracteristica> {
    
    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;
    
    public TipoCaracteristicaDAO() {
        super(TipoCaracteristica.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<TipoCaracteristica> root) {
        return List.of(
            cb.asc(root.get("nombre")), 
            cb.asc(root.get("idTipoCaracteristica"))
        );
    }

    public List<TipoCaracteristica> buscarPorNombre(String texto, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar los tipos de característica", () ->
            em.createNamedQuery("TipoCaracteristica.buscarPorNombre", TipoCaracteristica.class)
                .setParameter("texto", patronLike(texto))
                .setMaxResults(max)
                .getResultList()
        );
    }

    public List<TipoCaracteristica> buscarActivosPorNombre(String texto, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar los tipos de característica activos", () ->
            em.createNamedQuery("TipoCaracteristica.buscarActivosPorNombre", TipoCaracteristica.class)
                .setParameter("texto", patronLike(texto))
                .setMaxResults(max)
                .getResultList()
        );
    }

    // Métodos opcionales para filtrado directo por estado activo/inactivo
    public List<TipoCaracteristica> findByActivo(boolean activo, int first, int max) {
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        return ejecutar("No se pudieron listar los tipos de característica por estado", () ->
            em.createNamedQuery("TipoCaracteristica.findByActivo", TipoCaracteristica.class)
                .setParameter("activo", activo)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByActivo(boolean activo) {
        return ejecutar("No se pudo contar los tipos de característica por estado", () -> {
            Long count = em.createNamedQuery("TipoCaracteristica.countByActivo", Long.class)
                    .setParameter("activo", activo)
                    .getSingleResult();
            return count != null ? count.intValue() : 0;
        });
    }
}