package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@Dependent
public class ProductoCaracteristicaModel extends DefaultModel<ProductoCaracteristica> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoCaracteristicaDAO productoCaracteristicaDAO;

    @Inject
    private CaracteristicaDAO caracteristicaDAO;

    @Inject
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    private Producto producto;
    private TipoCaracteristica tipoCaracteristicaSeleccionado;

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
        return productoCaracteristicaDAO.findByProducto(producto, first, max);
    }

    @Override
    protected int contarDatos() {
        if (producto == null || producto.getIdProducto() == null) {
            return 0;
        }
        return productoCaracteristicaDAO.countByProducto(producto);
    }

    // Autocompletado para el selector de TipoCaracteristica (limitado a 10 registros).
    public List<TipoCaracteristica> completarTipoCaracteristica(String query) {
        try {
            return tipoCaracteristicaDAO.buscarActivosPorNombre(query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los tipos de característica", ex);
            return new ArrayList<>();
        }
    }

    // Autocompletado para seleccionar Caracteristica según el TipoCaracteristica activo.
    public List<Caracteristica> completarCaracteristicaPorTipo(String query) {
        if (this.tipoCaracteristicaSeleccionado == null || this.tipoCaracteristicaSeleccionado.getIdTipoCaracteristica() == null) {
            return Collections.emptyList();
        }
        try {
            return caracteristicaDAO.buscarActivasPorTipoYNombre(this.tipoCaracteristicaSeleccionado, query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar las características del tipo seleccionado", ex);
            return new ArrayList<>();
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
        return ".*";
    }

    @Override
    protected String validarRegistro(ProductoCaracteristica registro) {
        if (registro.getIdProducto() == null || registro.getIdProducto().getIdProducto() == null) {
            return "Debe seleccionar un producto válido";
        }

        Caracteristica c = registro.getIdCaracteristica();
        if (c == null || c.getIdCaracteristica() == null) {
            return "Debe seleccionar una característica válida";
        }

        if (!Boolean.TRUE.equals(c.getActivo())) {
            return "La característica seleccionada se encuentra inactiva";
        }

        if (registro.getValor() == null || registro.getValor().isBlank()) {
            return "El valor de la característica es obligatorio";
        }

        String regex = getRegexCaracteristica();
        if (!".*".equals(regex)) {
            try {
                if (!Pattern.matches(regex, registro.getValor())) {
                    return "El valor no cumple con la regla o patrón requerido: " + regex;
                }
            } catch (PatternSyntaxException ex) {
                return "La expresión regular asociada al tipo de característica es inválida";
            }
        }

        return null;
    }

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
}
