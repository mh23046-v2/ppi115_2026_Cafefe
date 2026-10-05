package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;

/**
 *
 * @author hernandez
 */
@Stateless
public class OrdenDAO extends AbstractDataAccess<Orden> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public OrdenDAO() {
        super(Orden.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    // Página del listado, de la orden más reciente a la más antigua. 
    public List<Orden> findRangeOrdenado(int first, int max) {
        return em.createNamedQuery("Orden.findAllOrdenado", Orden.class)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }

    /**
     * Asignaciones empleado-rol activas (empleado y rol también activos).
     * El filtro por rol admitido (camarero, barista...) se hace después con
     * ReglasOrden.puedeTomarOrdenes, para no repetir la regla en JPQL.
     */
    public List<EmpleadoRol> findEmpleadoRolActivos() {
        return em.createQuery(
                "SELECT er FROM EmpleadoRol er JOIN er.idEmpleado e JOIN er.idRol r"
                + " WHERE er.activo = true AND e.activo = true AND r.activo = true"
                + " ORDER BY e.nombre, e.apellido, r.nombre", EmpleadoRol.class)
                .getResultList();
    }

    /**
     * Borra la orden junto con sus productos (en la misma transacción).
     * Si algún producto ya está facturado, la BD rechaza el borrado y se
     * muestra el error en pantalla.
     */
    @Override
    public void delete(Orden orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }
        em.createQuery("DELETE FROM OrdenProducto op WHERE op.idOrden.idOrden = :idOrden")
                .setParameter("idOrden", orden.getIdOrden())
                .executeUpdate();
        super.delete(orden);
    }
}
