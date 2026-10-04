package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@Dependent
public class ProductoCaracteristicaModel extends DefaultModel<ProductoCaracteristica> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoCaracteristicaDAO productoCaracteristicaDAO;

    @Inject
    private CaracteristicaDAO caracteristicaDAO;

    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @Inject
    private Etiquetas etiquetas;

    private Producto producto;
    private TipoCaracteristica tipoCaracteristicaSeleccionado;

    private List<SelectItem> opcionesTipoCaracteristica;
    private List<SelectItem> opcionesCaracteristica;

    @Override
    protected AbstractDataAccess<ProductoCaracteristica> getDAO() {
        return productoCaracteristicaDAO;
    }

    @Override
    protected UUID getId(ProductoCaracteristica entidad) {
        return entidad.getIdProductoCaracteristica();
    }

    @Override
    protected ProductoCaracteristica nuevoRegistro() {
        ProductoCaracteristica nuevo = new ProductoCaracteristica(UUID.randomUUID());
        nuevo.setIdProducto(producto);
        this.tipoCaracteristicaSeleccionado = null;
        this.opcionesCaracteristica = Collections.emptyList();
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Característica de Producto";
    }

    @Override
    protected List<ProductoCaracteristica> cargarDatos(int first, int max) {
        if (producto == null || producto.getIdProducto() == null) {
            return Collections.emptyList();
        }
        return productoCaracteristicaDAO.findByProducto(producto);
    }

    @Override
    protected int contarDatos() {
        if (producto == null || producto.getIdProducto() == null) {
            return 0;
        }
        return productoCaracteristicaDAO.findByProducto(producto).size();
    }

    @Override
    protected void cargarOpciones() {
        // Cargar primer combo del modal (Tipos de Característica)
        opcionesTipoCaracteristica = new ArrayList<>();

        List<TipoCaracteristica> tipos = tipoCaracteristicaDAO.findAll();
        for (TipoCaracteristica tc : tipos) {
            // Se muestran todos, pero si no está activo, se deshabilita
            boolean deshabilitado = !Boolean.TRUE.equals(tc.getActivo());
            opcionesTipoCaracteristica.add(new SelectItem(tc, tc.getNombre(), null, deshabilitado));
        }
    }

// Filtro para obtener las características según el tipo de característica seleccionado
    public void cambioTipoCaracteristica() {
        opcionesCaracteristica = new ArrayList<>();
        if (this.tipoCaracteristicaSeleccionado != null) {
            List<Caracteristica> lista = caracteristicaDAO.findByIdTipoCaracteristica(this.tipoCaracteristicaSeleccionado);

            for (Caracteristica c : lista) {
                // Se muestran todas, pero si no está activa, se deshabilita
                boolean deshabilitada = !Boolean.TRUE.equals(c.getActivo());
                opcionesCaracteristica.add(new SelectItem(c, c.getNombre(), null, deshabilitada));
            }
        }
    }

    public String getRegexCaracteristica() {
        if (registro != null
                && registro.getIdCaracteristica() != null
                && registro.getIdCaracteristica().getIdTipoCaracteristica() != null) {

            String regex = registro.getIdCaracteristica().getIdTipoCaracteristica().getExpresionRegular();
            if (regex != null && !regex.isBlank()) {
                return regex;
            }
        }
        return ".*"; // Acepta cualquier texto si no hay regla definida
    }

    // Getters y setters para la vista
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public TipoCaracteristica getTipoCaracteristicaSeleccionado() {
        return tipoCaracteristicaSeleccionado;
    }

    public void setTipoCaracteristicaSeleccionado(TipoCaracteristica tipoCaracteristicaSeleccionado) {
        this.tipoCaracteristicaSeleccionado = tipoCaracteristicaSeleccionado;
    }

    public List<SelectItem> getOpcionesTipoCaracteristica() {
        return opcionesTipoCaracteristica;
    }

    public List<SelectItem> getOpcionesCaracteristica() {
        return opcionesCaracteristica;
    }
}
