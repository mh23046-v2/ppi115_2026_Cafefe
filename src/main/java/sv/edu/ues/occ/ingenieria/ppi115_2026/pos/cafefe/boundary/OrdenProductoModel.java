package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasOrden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

/**
 *
 * @author hernandez
 */
// Es dependiente porque existe dentro de la página de Orden
@Dependent
public class OrdenProductoModel extends DefaultModel<OrdenProducto> {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenProductoDAO ordenProductoDAO;

    private Orden orden;

    // Diálogo "Seleccionar Producto"
    private List<Producto> productosDisponibles;

    // Diálogo "Aplicar Descuento"
    private List<SelectItem> opcionesDescuento;
    private Descuento descuentoSeleccionado;

    @Override
    protected AbstractDataAccess<OrdenProducto> getDAO() {
        return ordenProductoDAO;
    }

    @Override
    protected UUID getId(OrdenProducto entidad) {
        return entidad.getIdOrdenProducto();
    }

    @Override
    protected OrdenProducto nuevoRegistro() {
        OrdenProducto nuevo = new OrdenProducto(UUID.randomUUID());
        nuevo.setIdOrden(orden);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Producto";
    }

    // Solo los productos de la orden seleccionada
    @Override
    protected List<OrdenProducto> cargarDatos(int first, int max) {
        if (!hayOrden()) {
            return Collections.emptyList();
        }
        return ordenProductoDAO.findByIdOrden(orden.getIdOrden(), first, max);
    }

    @Override
    protected int contarDatos() {
        if (!hayOrden()) {
            return 0;
        }
        return (int) ordenProductoDAO.countByIdOrden(orden.getIdOrden());
    }

    @Override
    protected void cargarOpciones() {
        productosDisponibles = ordenProductoDAO.findProductosActivos();
    }

    // ---------------- Seleccionar producto ----------------

    /** Botón "Seleccionar" del diálogo: pone el producto y su precio sugerido. */
    public void seleccionarProducto(Producto producto) {
        if (registro != null && producto != null) {
            registro.setIdProducto(producto);
            registro.setPrecio(producto.getPrecioSugerido());
        }
    }

    String validar(OrdenProducto op) {
        if (op.getIdOrden() == null) {
            return "Guarde primero la orden";
        }
        if (op.getIdProducto() == null) {
            return "Seleccione un producto";
        }
        if (op.getPrecio() == null || op.getPrecio().signum() < 0) {
            return "El producto no tiene un precio válido";
        }
        return null;
    }

    @Override
    public void btnCrearHandler() {
        if (registro != null && !validacionCorrecta()) {
            return;
        }
        super.btnCrearHandler();
    }

    @Override
    public void btnModificarHandler() {
        if (registro != null && !validacionCorrecta()) {
            return;
        }
        super.btnModificarHandler();
    }

    private boolean validacionCorrecta() {
        String error = validar(registro);
        if (error != null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Producto no guardado", error);
            return false;
        }
        return true;
    }

    // ---------------- Aplicar descuento ----------------

    /**
     * Descuentos que se pueden aplicar HOY a esta orden, agrupados por
     * descuento: solo los asignados a productos de la orden y vigentes en
     * la fecha de la orden.
     */
    Map<Descuento, List<DescuentoProducto>> descuentosAplicables() {
        Map<Descuento, List<DescuentoProducto>> resultado = new LinkedHashMap<>();
        if (!hayOrden()) {
            return resultado;
        }
        for (DescuentoProducto dp : ordenProductoDAO.findDescuentosDeProductosEnOrden(orden.getIdOrden())) {
            if (ReglasOrden.descuentoAplicable(dp, orden.getFechaCreacion())) {
                resultado.computeIfAbsent(dp.getIdDescuento(), d -> new ArrayList<>()).add(dp);
            }
        }
        return resultado;
    }

    /**
     * Botón "Aplicar Descuento": llena el combo antes de abrir el diálogo.
     * Lista TODOS los descuentos asignados a los productos de la orden; los
     * que no están vigentes en la fecha de la orden salen deshabilitados
     * (se ven, pero no se pueden seleccionar).
     */
    public void prepararDescuentos() {
        descuentoSeleccionado = null;
        opcionesDescuento = new ArrayList<>();
        if (!hayOrden()) {
            return;
        }
        Map<Descuento, List<DescuentoProducto>> vigentes = descuentosAplicables();
        Map<Descuento, List<DescuentoProducto>> todos = new LinkedHashMap<>();
        for (DescuentoProducto dp : ordenProductoDAO.findDescuentosDeProductosEnOrden(orden.getIdOrden())) {
            if (dp != null && dp.getIdDescuento() != null) {
                todos.computeIfAbsent(dp.getIdDescuento(), d -> new ArrayList<>()).add(dp);
            }
        }
        boolean hayVigente = false;
        for (Map.Entry<Descuento, List<DescuentoProducto>> e : todos.entrySet()) {
            List<DescuentoProducto> lista = vigentes.get(e.getKey());
            boolean vigente = lista != null && !lista.isEmpty();
            hayVigente |= vigente;
            String nombre = e.getKey().getNombre() == null ? "" : e.getKey().getNombre();
            SelectItem item = new SelectItem(e.getKey(),
                    vigente ? etiquetaDescuento(e.getKey(), lista) : nombre + " (fuera de vigencia)");
            item.setDisabled(!vigente);
            opcionesDescuento.add(item);
        }
        if (todos.isEmpty()) {
            mensaje(FacesMessage.SEVERITY_WARN, "Sin descuentos",
                    "Los productos de la orden no tienen descuentos asignados");
        } else if (!hayVigente) {
            mensaje(FacesMessage.SEVERITY_WARN, "Sin descuentos vigentes",
                    "Ningún descuento está vigente en la fecha de la orden");
        }
    }

    /** "Cliente Platinum (20)%"; si cambia según el producto: "(hasta 20)%". */
    String etiquetaDescuento(Descuento d, List<DescuentoProducto> lista) {
        int min = lista.stream().mapToInt(ReglasOrden::porcentajeEfectivo).min().orElse(0);
        int max = lista.stream().mapToInt(ReglasOrden::porcentajeEfectivo).max().orElse(0);
        String nombre = d.getNombre() == null ? "" : d.getNombre();
        return nombre + (min == max ? " (" + max + ")%" : " (hasta " + max + ")%");
    }

    /**
     * Aplica el descuento elegido: a cada producto de la orden que lo tenga,
     * su precio pasa a ser precio sugerido − porcentaje. Se calcula siempre
     * desde el precio sugerido, así aplicar dos veces no descuenta doble.
     * La vigencia se vuelve a revisar aquí (no se confía solo en el combo).
     */
    public void aplicarDescuento() {
        if (!hayOrden() || descuentoSeleccionado == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Descuento no aplicado", "Seleccione un descuento");
            return;
        }
        List<DescuentoProducto> vigentes = descuentosAplicables().get(descuentoSeleccionado);
        if (vigentes == null || vigentes.isEmpty()) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Descuento no aplicado",
                    "El descuento no está vigente en la fecha de la orden");
            return;
        }
        Map<Producto, DescuentoProducto> porProducto = new LinkedHashMap<>();
        for (DescuentoProducto dp : vigentes) {
            porProducto.put(dp.getIdProducto(), dp);
        }
        int aplicados = 0;
        try {
            for (OrdenProducto op : ordenProductoDAO.findByIdOrden(orden.getIdOrden())) {
                DescuentoProducto dp = porProducto.get(op.getIdProducto());
                if (dp != null && op.getIdProducto().getPrecioSugerido() != null) {
                    op.setPrecio(ReglasOrden.precioConDescuento(
                            op.getIdProducto().getPrecioSugerido(), ReglasOrden.porcentajeEfectivo(dp)));
                    ordenProductoDAO.modify(op);
                    aplicados++;
                }
            }
            mensaje(FacesMessage.SEVERITY_INFO, "Descuento aplicado",
                    descuentoSeleccionado.getNombre() + " se aplicó a " + aplicados + " producto(s)");
            descuentoSeleccionado = null;
        } catch (Exception ex) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Descuento no aplicado", ex.getMessage());
        }
    }
    
    /** Texto del descuento aplicado al producto de la orden, ej. "-20% (-$0.50)". Vacío si no tiene. */
    public String descuentoAplicado(OrdenProducto op) {
        if (op == null || op.getPrecio() == null || op.getIdProducto() == null
                || op.getIdProducto().getPrecioSugerido() == null) {
            return "";
        }
        java.math.BigDecimal base = op.getIdProducto().getPrecioSugerido();
        if (base.signum() <= 0 || op.getPrecio().compareTo(base) >= 0) {
            return "";
        }
        java.math.BigDecimal ahorro = base.subtract(op.getPrecio());
        java.math.BigDecimal pct = ahorro.multiply(java.math.BigDecimal.valueOf(100))
                .divide(base, 0, java.math.RoundingMode.HALF_UP);
        return "-" + pct + "% (-$" + ahorro.setScale(2, java.math.RoundingMode.HALF_UP) + ")";
    }

    // ---------------- utilidades ----------------

    private boolean hayOrden() {
        return orden != null && orden.getIdOrden() != null;
    }

    private void mensaje(FacesMessage.Severity severidad, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, resumen, detalle));
    }

    /** Total de la orden (suma de precios ya con descuento). */
    public BigDecimal getTotal() {
        return hayOrden() ? ordenProductoDAO.totalByIdOrden(orden.getIdOrden()) : BigDecimal.ZERO;
    }

    // ---- getters y setters para la vista ----

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public List<Producto> getProductosDisponibles() {
        return productosDisponibles;
    }

    public List<SelectItem> getOpcionesDescuento() {
        return opcionesDescuento;
    }

    public Descuento getDescuentoSeleccionado() {
        return descuentoSeleccionado;
    }

    public void setDescuentoSeleccionado(Descuento descuentoSeleccionado) {
        this.descuentoSeleccionado = descuentoSeleccionado;
    }
}
