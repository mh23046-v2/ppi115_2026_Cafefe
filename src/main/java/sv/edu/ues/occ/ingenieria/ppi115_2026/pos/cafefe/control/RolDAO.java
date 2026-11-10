package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

@Stateless
public class RolDAO extends AbstractDataAccess<Rol> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public RolDAO() {
        super(Rol.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<Rol> root) {
        return List.of(
            cb.desc(root.get("activo")),
            cb.asc(root.get("nombre"))
        );
    }

    public List<Rol> findByNombre(String texto, int first, int max) {
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        String filtro = patronLike(texto);
        return ejecutar("No se pudieron buscar los roles por nombre", () ->
            em.createNamedQuery("Rol.findByNombre", Rol.class)
                .setParameter("nombre", filtro)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public List<Rol> findByNombre(String texto, int max) {
        return findByNombre(texto, 0, max);
    }

    public Long countByNombre(String texto) {
        String filtro = patronLike(texto);
        return ejecutar("No se pudo contar los roles por nombre", () ->
            em.createNamedQuery("Rol.countByNombre", Long.class)
                .setParameter("nombre", filtro)
                .getSingleResult()
        );
    }

    /** Roles activos (tabla pequeña): sirve para filtrar por nombre de rol con tildes/mayúsculas. */
    public List<Rol> findActivos() {
        return ejecutar("No se pudieron consultar los roles activos", () ->
            em.createNamedQuery("Rol.findActivos", Rol.class).getResultList()
        );
    }

    /** Para autocompletes: solo los registros activos, filtrados en la consulta. */
    public List<Rol> buscarActivosPorNombre(String texto, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar los roles activos", () ->
            em.createNamedQuery("Rol.buscarActivosPorNombre", Rol.class)
                .setParameter("nombre", patronLike(texto))
                .setMaxResults(max)
                .getResultList()
        );
    }
}
