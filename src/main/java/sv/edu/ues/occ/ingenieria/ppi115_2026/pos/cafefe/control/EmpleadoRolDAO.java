package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;

@Stateless
public class EmpleadoRolDAO extends AbstractDataAccess<EmpleadoRol> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public EmpleadoRolDAO() {
        super(EmpleadoRol.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<EmpleadoRol> root) {
        return List.of(
                cb.desc(root.get("activo"))
        );
    }

    public List<EmpleadoRol> findByEmpleado(Empleado empleado, int first, int max) {
        if (empleado == null || empleado.getIdEmpleado() == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo ni estar sin ID");
        }
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        return ejecutar("No se pudieron buscar los roles del empleado", ()
                -> em.createNamedQuery("EmpleadoRol.findByEmpleado", EmpleadoRol.class)
                        .setParameter("empleado", empleado)
                        .setFirstResult(first)
                        .setMaxResults(max)
                        .getResultList()
        );
    }

    public int countByEmpleado(Empleado empleado) {
        if (empleado == null || empleado.getIdEmpleado() == null) {
            return 0;
        }
        return ejecutar("No se pudo contar los roles del empleado", () -> {
            Long count = em.createNamedQuery("EmpleadoRol.countByEmpleado", Long.class)
                    .setParameter("empleado", empleado)
                    .getSingleResult();
            return count != null ? count.intValue() : 0;
        });
    }

    public List<EmpleadoRol> findByEmpleadoNombre(String texto, int first, int max) {
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        String filtro = (texto == null || texto.trim().isEmpty()) ? "%" : "%" + texto.trim().toLowerCase() + "%";

        return ejecutar("No se pudieron buscar las asignaciones por nombre", ()
                -> em.createNamedQuery("EmpleadoRol.findByEmpleadoNombre", EmpleadoRol.class)
                        .setParameter("nombre", filtro)
                        .setFirstResult(first)
                        .setMaxResults(max)
                        .getResultList()
        );
    }

    public List<EmpleadoRol> findByEmpleadoNombre(String texto, int max) {
        return findByEmpleadoNombre(texto, 0, max);
    }
}
