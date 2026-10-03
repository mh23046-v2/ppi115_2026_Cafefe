package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

/**
 *
 * @author hernandez
 */
@Named
@ViewScoped
public class TipoProductoModel extends DefaultModel<TipoProducto> {

    private static final long serialVersionUID = 1L;

    @Inject
    private TipoProductoDAO tipoProductoDAO;
    
    @Override
    protected AbstractDataAccess<TipoProducto> getDAO() {
        return tipoProductoDAO;
    }

    @Override
    protected UUID getId(TipoProducto entidad) {
        return entidad.getIdTipoProducto();
    }

    @Override
    protected TipoProducto nuevoRegistro() {
        TipoProducto nuevo = new TipoProducto(UUID.randomUUID());
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Tipo de Producto";
    }
    
}
