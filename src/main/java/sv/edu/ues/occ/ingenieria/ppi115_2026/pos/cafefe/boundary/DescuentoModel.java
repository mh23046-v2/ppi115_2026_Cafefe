package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

/**
 *
 * @author johnyv
 */
@Named
@ViewScoped
public class DescuentoModel extends DefaultModel<Descuento> { 
    
    private static final long serialVersionUID = 1L;

    @Inject
    private DescuentoDAO descuentoDAO;
    @Inject
    private TipoDescuentoDAO tipoDescuentoDAO;
    @Inject
    private Etiquetas etiquetas;

    private List<SelectItem> opcionesIdTipoDescuento;

    @Override
    protected AbstractDataAccess<Descuento> getDAO() {
        return descuentoDAO;
    }

    @Override
    protected UUID getId(Descuento entidad) {
        return entidad.getIdDescuento();
    }

    @Override
    protected Descuento nuevoRegistro() {
        Descuento nuevo = new Descuento(UUID.randomUUID());
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Descuento";
    }

    @Override
    protected void cargarOpciones() {
        opcionesIdTipoDescuento = new ArrayList<>();
        for (TipoDescuento tD : tipoDescuentoDAO.findAll()) {
            opcionesIdTipoDescuento.add(new SelectItem(tD, etiquetas.tipoDescuento(tD), null, !tD.getActivo()));
        }
    }

    public List<SelectItem> getOpcionesIdTipoDescuento() {
        return opcionesIdTipoDescuento;
    }
}
