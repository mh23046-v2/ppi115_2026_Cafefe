package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoTipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

/**
 *
 * @author johnyv
 */
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
    private List<SelectItem> opcionesTipoProducto;

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
    public void setRegistro(ProductoTipoProducto registro) {
        super.setRegistro(registro);
        cargarOpciones();
    }

    @Override
    protected List<ProductoTipoProducto> cargarDatos(int first, int max) {
        if (producto == null || producto.getIdProducto() == null) {
            return Collections.emptyList();
        }
        return productoTipoProductoDAO.findByProducto(producto);
    }

    @Override
    protected int contarDatos() {
        if (producto == null || producto.getIdProducto() == null) {
            return 0;
        }
        return productoTipoProductoDAO.findByProducto(producto).size();
    }

    @Override
    protected void cargarOpciones() {
        opcionesTipoProducto = new ArrayList<>();

        if (this.producto != null && this.producto.getIdProducto() != null) {

            List<ProductoTipoProducto> asignados = productoTipoProductoDAO.findByProducto(this.producto);

            Set<UUID> idsAsignados = asignados.stream()
                    .filter(ptp -> ptp != null && ptp.getIdTipoProducto() != null)
                    .map(ptp -> ptp.getIdTipoProducto().getIdTipoProducto())
                    .collect(Collectors.toSet());

            UUID idTipoEnEdicion = null;
            if (this.registro != null && this.registro.getIdTipoProducto() != null) {
                idTipoEnEdicion = this.registro.getIdTipoProducto().getIdTipoProducto();
            }

            for (TipoProducto tp : tipoProductoDAO.findAll()) {
                boolean esElSeleccionadoActualmente = (idTipoEnEdicion != null && idTipoEnEdicion.equals(tp.getIdTipoProducto()));

                if (!idsAsignados.contains(tp.getIdTipoProducto()) || esElSeleccionadoActualmente) {
                    opcionesTipoProducto.add(new SelectItem(tp, etiquetas.tipoProducto(tp), null, !tp.getActivo()));
                }
            }
        } else {
            for (TipoProducto tp : tipoProductoDAO.findAll()) {
                opcionesTipoProducto.add(new SelectItem(tp, etiquetas.tipoProducto(tp), null, !tp.getActivo()));
            }
        }
    }

    // Getters y setters para la vista
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        cargarOpciones();
    }

    public List<SelectItem> getOpcionesTipoProducto() {
        return opcionesTipoProducto;
    }
}
