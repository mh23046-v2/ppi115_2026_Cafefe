package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
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
}
