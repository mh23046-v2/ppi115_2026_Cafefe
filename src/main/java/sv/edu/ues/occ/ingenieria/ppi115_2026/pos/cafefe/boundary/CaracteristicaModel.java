package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author johnyv
 */
@Named
@ViewScoped
public class CaracteristicaModel extends DefaultModel<Caracteristica> {
    
    private static final long serialVersionUID = 1L;

    @Inject
    private CaracteristicaDAO caracteristicaDAO;
    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;
    @Inject
    private Etiquetas etiquetas;

    private List<SelectItem> opcionesIdTipoCaracteristica;

    @Override
    protected AbstractDataAccess<Caracteristica> getDAO() {
        return caracteristicaDAO;
    }

    @Override
    protected UUID getId(Caracteristica entidad) {
        return entidad.getIdCaracteristica();
    }

    @Override
    protected Caracteristica nuevoRegistro() {
        Caracteristica nuevo = new Caracteristica(UUID.randomUUID());
        nuevo.setActivo(true);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Característica";
    }

    /** Opciones de los desplegables de las relaciones. */
    @Override
    protected void cargarOpciones() {
        opcionesIdTipoCaracteristica = new ArrayList<>();
        for (TipoCaracteristica  tC: tipoCaracteristicaDAO.findAll()) {
            opcionesIdTipoCaracteristica.add(new SelectItem(tC, etiquetas.tipoCaracteristica(tC)));
        }
    }

    public List<SelectItem> getOpcionesIdTipoCaracteristica() {
        return opcionesIdTipoCaracteristica;
    }
}
