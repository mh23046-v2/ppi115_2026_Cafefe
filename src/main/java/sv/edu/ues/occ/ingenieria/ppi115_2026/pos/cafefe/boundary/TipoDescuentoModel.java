package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

/**
 *
 * @author johnyv
 */
@Named
@ViewScoped
public class TipoDescuentoModel extends DefaultModel<TipoDescuento> {
    
    private static final long serialVersionUID = 1L;

    @Inject
    private TipoDescuentoDAO tipoDescuentoDAO;

    @Override
    protected AbstractDataAccess<TipoDescuento> getDAO() {
        return tipoDescuentoDAO;
    }

    @Override
    protected UUID getId(TipoDescuento entidad) {
        return entidad.getIdTipoDescuento();
    }

    @Override
    protected TipoDescuento nuevoRegistro() {
        TipoDescuento nuevo = new TipoDescuento(UUID.randomUUID());
        nuevo.setActivo(true);
        nuevo.setDescuentoMaximo(50);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Tipo de Descuento";
    }
}
