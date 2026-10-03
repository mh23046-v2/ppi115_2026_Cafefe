package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author johnyv
 */

/**
 * Model de la pantalla de mantenimiento de TipoCaracteristica.
 * Toda la lógica CRUD (listado paginado, crear, modificar, eliminar)
 * se hereda de {@link DefaultModel}; aquí solo va lo propio de la entidad.
 */
@Named
@ViewScoped
public class TipoCaracteristicaModel extends DefaultModel<TipoCaracteristica> {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;
    
    @Override
    protected AbstractDataAccess<TipoCaracteristica> getDAO() {
        return tipoCaracteristicaDAO;
    }
    
    @Override
    protected UUID getId(TipoCaracteristica entidad) {
        return entidad.getIdTipoCaracteristica();
    }
    
    @Override
    protected TipoCaracteristica nuevoRegistro() {
        TipoCaracteristica nuevo = new TipoCaracteristica(UUID.randomUUID());
        nuevo.setActivo(true);
        nuevo.setExpresionRegular(".*");
        return nuevo;
    }
    
    @Override
    public String nombreBean() {
        return "Tipo de Característica";
    }
}
