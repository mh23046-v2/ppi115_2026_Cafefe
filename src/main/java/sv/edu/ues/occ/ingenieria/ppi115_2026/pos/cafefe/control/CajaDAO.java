package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;

@Stateless
public class CajaDAO extends AbstractDataAccess<Caja> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public CajaDAO() {
        super(Caja.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public List<Caja> findByActivo(Boolean activo, int first, int max) {
        if (activo == null) {
            throw new IllegalArgumentException("El estado activo es obligatorio");
        }
        validarRango(first, max);

        return ejecutar("No se pudieron cargar las cajas por estado activo", () ->
            em.createNamedQuery("Caja.findByActivo", Caja.class)
                .setParameter("activo", activo)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public int countByActivo(Boolean activo) {
        if (activo == null) {
            throw new IllegalArgumentException("El estado activo es obligatorio");
        }

        return ejecutar("No se pudo contar las cajas por estado activo", () -> {
            Long total = em.createNamedQuery("Caja.countByActivo", Long.class)
                .setParameter("activo", activo)
                .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }

    /** Para el autocomplete de la factura: solo cajas activas, filtradas en la consulta. */
    public List<Caja> buscarActivasPorNombre(String texto, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar las cajas activas", () ->
            em.createNamedQuery("Caja.buscarActivasPorNombre", Caja.class)
                .setParameter("texto", patronLike(texto))
                .setMaxResults(max)
                .getResultList()
        );
    }

    // Orden estable para la tabla: por nombre y, a igual nombre, por id.
    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<Caja> root) {
        return List.of(cb.asc(root.get("nombre")), cb.asc(root.get("idCaja")));
    }

    /**
     * ¿Ya existe otra caja con ese nombre? Compara sin mayúsculas ni espacios
     * en los extremos y excluye la propia caja (por si se está modificando).
     */
    public boolean existeNombre(String nombre, UUID excluir) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        UUID idExcluido = excluir != null ? excluir : new UUID(0L, 0L);
        return ejecutar("No se pudo verificar el nombre de la caja", () ->
            em.createNamedQuery("Caja.countByNombreExcluyendo", Long.class)
                .setParameter("nombre", nombre.trim().toLowerCase())
                .setParameter("excluir", idExcluido)
                .getSingleResult() > 0
        );
    }
}
