package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

/**
 *
 * @author johnyv
 */
@Named
@ViewScoped
public class ProductoModel extends DefaultModel<Producto> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoDAO productoDAO;

    @Inject
    private ProductoTipoProductoModel productoTipoProductoModel;

    @Inject
    private ProductoCaracteristicaModel productoCaracteristicaModel;

    @Inject
    private DescuentoProductoModel descuentoProductoModel;

    // Pestaña activa del p:tabView (0 = Generalidades, 1 = Tipos, 2 = Características, 3 = Descuentos)
    private int tabActiva;

    @Override
    protected AbstractDataAccess<Producto> getDAO() {
        return productoDAO;
    }

    @Override
    protected UUID getId(Producto entidad) {
        return entidad.getIdProducto();
    }

    @Override
    protected Producto nuevoRegistro() {
        Producto nuevo = new Producto();
        nuevo.setIdProducto(UUID.randomUUID());
        nuevo.setActivo(true);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Producto";
    }

    @Override
    protected void registroCambio() {
        // Sincronizamos el producto seleccionado con los tres modelos hijos
        productoTipoProductoModel.setProducto(registro);
        productoTipoProductoModel.btnCancelarHandler();

        productoCaracteristicaModel.setProducto(registro);
        productoCaracteristicaModel.btnCancelarHandler();

        descuentoProductoModel.setProducto(registro);
        descuentoProductoModel.btnCancelarHandler();

        tabActiva = 0;
    }

    // Getters y setters para la vista
    public ProductoTipoProductoModel getProductoTipoProductoModel() {
        return productoTipoProductoModel;
    }

    public ProductoCaracteristicaModel getProductoCaracteristicaModel() {
        return productoCaracteristicaModel;
    }

    public DescuentoProductoModel getDescuentoProductoModel() {
        return descuentoProductoModel;
    }

    public int getTabActiva() {
        return tabActiva;
    }

    public void setTabActiva(int tabActiva) {
        this.tabActiva = tabActiva;
    }
}
