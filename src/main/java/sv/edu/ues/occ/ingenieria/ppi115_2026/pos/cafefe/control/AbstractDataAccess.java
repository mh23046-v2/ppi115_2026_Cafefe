package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

/**
 *
 * @author hernandez
 */
public abstract class AbstractDataAccess<T> {

    private final Class<T> entityClass;

    protected AbstractDataAccess(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    public void create(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }
        getEntityManager().persist(entity);
    }

    public T modify(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }
        return getEntityManager().merge(entity);
    }

    public void delete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }
        EntityManager em = getEntityManager();
        // Si la entidad viene "desconectada" se re-adjunta antes de borrar
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public T findById(Object id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser nulo");
        }
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        EntityManager em = getEntityManager();
        CriteriaQuery<T> cq = em.getCriteriaBuilder().createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    //Paginación: devuelve hasta 'max' registros a partir de la posición 'first'.
    public List<T> findRange(int first, int max) {
        EntityManager em = getEntityManager();
        CriteriaQuery<T> cq = em.getCriteriaBuilder().createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        cq.select(root);
        return em.createQuery(cq)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }

    public long count() {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }
}