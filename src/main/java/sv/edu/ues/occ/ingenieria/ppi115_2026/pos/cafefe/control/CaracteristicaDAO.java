package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    public List<Caracteristica> findByActivo(Boolean activo) {
        return em.createNamedQuery("Caracteristica.findByActivo", Caracteristica.class)
                .setParameter("activo", activo)
                .getResultList();
    }

    public List<Caracteristica> findByNombre(String nombre) {
        return em.createNamedQuery("Caracteristica.findByNombre", Caracteristica.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }

    public List<Caracteristica> findByIdTipoCaracteristica(TipoCaracteristica idTipoCaracteristica) {
        if (idTipoCaracteristica == null || idTipoCaracteristica.getIdTipoCaracteristica() == null) {
            return List.of();
        }
        return em.createQuery("SELECT c FROM Caracteristica c WHERE c.idTipoCaracteristica = :idTipoCaracteristica", Caracteristica.class)
                .setParameter("idTipoCaracteristica", idTipoCaracteristica)
                .getResultList();
    }
}