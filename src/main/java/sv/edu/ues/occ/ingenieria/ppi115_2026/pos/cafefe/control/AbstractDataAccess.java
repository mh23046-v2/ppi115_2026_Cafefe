package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractDataAccess<T> {

    private static final Logger LOG = Logger.getLogger(AbstractDataAccess.class.getName());
    private final Class<T> entityClass;

    protected AbstractDataAccess(Class<T> entityClass) { this.entityClass = entityClass; }
    protected abstract EntityManager getEntityManager();

    // Método para manejar el try/catch en cada otra función
    protected <R> R ejecutar(String mensaje, Supplier<R> accion) {
        try {
            return accion.get();
        } catch (PersistenceException ex) {
            LOG.log(Level.SEVERE, mensaje + " [" + entityClass.getSimpleName() + "]", ex);
            throw new AccesoDatosException(mensaje, ex);
        }
    }
    
    public void create(T entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");
        ejecutar("No se pudo crear el registro", () -> {
            getEntityManager().persist(entity);
            getEntityManager().flush();
            return null;
        });
    }

    public T modify(T entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");
        return ejecutar("No se pudo modificar el registro", () -> {
            T merged = getEntityManager().merge(entity);
            getEntityManager().flush();
            return merged;
        });
    }

    public void delete(T entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");
        ejecutar("No se pudo eliminar el registro", () -> {
            EntityManager em = getEntityManager();
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            em.flush();
            return null;
        });
    }

    public T findById(Object id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        return ejecutar("No se pudo consultar el registro por id", () -> 
            getEntityManager().find(entityClass, id)
        );
    }

    @Deprecated
    public List<T> findAll() {
        return ejecutar("No se pudieron consultar todos los registros", () -> {
            EntityManager em = getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            
            List<jakarta.persistence.criteria.Order> orden = ordenar(cb, root);
            if (orden != null && !orden.isEmpty()) {
                cq.orderBy(orden);
            }
            
            cq.select(root);
            return em.createQuery(cq).getResultList();
        });
    }

    public List<T> findRange(int first, int max) {
        validarRango(first, max);
        return ejecutar("No se pudieron consultar los registros paginados", () -> {
            EntityManager em = getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            
            cq.select(root);
            List<jakarta.persistence.criteria.Order> orden = ordenar(cb, root);
            if (orden != null && !orden.isEmpty()) {
                cq.orderBy(orden);
            }
            
            return em.createQuery(cq)
                    .setFirstResult(first)
                    .setMaxResults(max)
                    .getResultList();
        });
    }

    public long count() {
        return ejecutar("No se pudo contar el total de registros", () -> {
            EntityManager em = getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);
            cq.select(cb.count(root));
            return em.createQuery(cq).getSingleResult();
        });
    }
    
    protected void validarRango(int first, int max) {
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Rango de paginación no válido");
        }
    }
    
    protected List<jakarta.persistence.criteria.Order> ordenar(CriteriaBuilder cb, Root<T> root) { 
        return List.of(); 
    }
    
    protected static String patronLike(String texto) {
        return "%" + (texto == null ? "" : texto.trim().toLowerCase()) + "%";
    }
}