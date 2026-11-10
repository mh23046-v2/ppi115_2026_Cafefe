package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

@Dependent
public class ProductoTipoProductoModel extends DefaultModel<ProductoTipoProducto> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @Inject
    private TipoProductoDAO tipoProductoDAO;

    @Inject
    private Etiquetas etiquetas;

    private Producto producto;

    @Override
    protected AbstractDataAccess<ProductoTipoProducto> getDAO() {
        return productoTipoProductoDAO;
    }

    @Override
    protected UUID getId(ProductoTipoProducto entidad) {
        return entidad.getIdProductoTipoProducto();
    }

    @Override
    protected ProductoTipoProducto nuevoRegistro() {
        ProductoTipoProducto nuevo = new ProductoTipoProducto(UUID.randomUUID());
        nuevo.setProducto(producto);
        nuevo.setFechaCreacion(new Date());
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Tipo de Producto";
    }

    @Override
    protected String validarRegistro(ProductoTipoProducto registro) {
        if (registro.getIdTipoProducto() == null) {
            return "El tipo de producto es obligatorio";
        }
        if (registro.getIdTipoProducto().getActivo() != null && !registro.getIdTipoProducto().getActivo()) {
            return "El tipo de producto seleccionado se encuentra inactivo";
        }
        // Antes lo evitaba el combo (ocultaba los ya asignados); con el autocomplete se valida aquí.
        if (productoTipoProductoDAO.existeAsignacion(registro.getProducto(), registro.getIdTipoProducto(),
                registro.getIdProductoTipoProducto())) {
            return "El producto ya tiene asignado ese tipo";
        }
        return null;
    }

    @Override
    protected List<ProductoTipoProducto> cargarDatos(int first, int max) {
        if (producto == null || producto.getIdProducto() == null) {
            return Collections.emptyList();
        }
        return productoTipoProductoDAO.findByProducto(producto, first, max);
    }

    @Override
    protected int contarDatos() {
        if (producto == null || producto.getIdProducto() == null) {
            return 0;
        }
        Long total = productoTipoProductoDAO.countByProducto(producto);
        return total != null ? total.intValue() : 0;
    }

    // Getters y setters para la vista
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public List<TipoProducto> completarTipoProducto(String query) {
        try {
            return tipoProductoDAO.buscarActivosPorNombre(query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los tipos de producto", ex);
            return new ArrayList<>();
        }
    }
}
