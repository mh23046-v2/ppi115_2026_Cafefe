package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasOrden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@Dependent
public class OrdenProductoModel extends DefaultModel<OrdenProducto> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenProductoDAO ordenProductoDAO;

    @Inject
    private ProductoDAO productoDAO;

    @Inject
    private EmpleadoRolDAO empleadoRolDAO;

    private Orden orden;

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

    @Override
    protected List<OrdenProducto> cargarDatos(int first, int max) {
        if (!hayOrden()) {
            return Collections.emptyList();
        }
        return ordenProductoDAO.findByOrden(orden, first, max);
    }

    @Override
    protected int contarDatos() {
        if (!hayOrden()) {
            return 0;
        }
        return ordenProductoDAO.countByOrden(orden);
    }

    @Override
    protected String validarRegistro(OrdenProducto op) {
        if (op == null) {
            return "El registro no puede ser nulo";
        }
        if (op.getIdOrden() == null || op.getIdOrden().getIdOrden() == null) {
            return "Guarde primero la orden";
        }
        if (op.getIdProducto() == null || op.getIdProducto().getIdProducto() == null) {
            return "Seleccione un producto";
        }
        if (op.getPrecio() == null || op.getPrecio().signum() < 0) {
            return "El producto no tiene un precio válido";
        }
        return null;
    }

    // Autocompletado acotado para seleccionar únicamente productos activos para la venta.
    public List<EmpleadoRol> completarEmpleadoRol(String query) {
        try {
            List<EmpleadoRol> resultados = empleadoRolDAO.findByEmpleadoNombre(query, 0, 25);
            return resultados.stream()
                    .filter(ReglasOrden::puedeTomarOrdenes)
                    .limit(10)
                    .toList();
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los empleados aptos para tomar la orden", ex);
            return new ArrayList<>();
        }
    }

    // Selección de producto vía autocompletado: setea el producto y asigna su precio sugerido por defecto.
    public void seleccionarProducto(Producto producto) {
        if (registro != null && producto != null) {
            registro.setIdProducto(producto);
            registro.setPrecio(producto.getPrecioSugerido());
        }
    }

    public List<Producto> completarProducto(String query) {
        try {
            return productoDAO.findByNombreActivos(query, 0, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los productos disponibles", ex);
            return new ArrayList<>();
        }
    }

    // ---------------- Aplicar descuento ----------------
    Map<Descuento, List<DescuentoProducto>> descuentosAplicables() {
        Map<Descuento, List<DescuentoProducto>> resultado = new LinkedHashMap<>();
        if (!hayOrden()) {
            return resultado;
        }
        for (DescuentoProducto dp : ordenProductoDAO.findDescuentosDeProductosEnOrden(orden)) {
            if (ReglasOrden.descuentoAplicable(dp, orden.getFechaCreacion())) {
                resultado.computeIfAbsent(dp.getIdDescuento(), d -> new ArrayList<>()).add(dp);
            }
        }
        return resultado;
    }

    public void prepararDescuentos() {
        descuentoSeleccionado = null;
        opcionesDescuento = new ArrayList<>();
        if (!hayOrden()) {
            return;
        }
        Map<Descuento, List<DescuentoProducto>> vigentes = descuentosAplicables();
        Map<Descuento, List<DescuentoProducto>> todos = new LinkedHashMap<>();
        for (DescuentoProducto dp : ordenProductoDAO.findDescuentosDeProductosEnOrden(orden)) {
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

    String etiquetaDescuento(Descuento d, List<DescuentoProducto> lista) {
        int min = lista.stream().mapToInt(ReglasOrden::porcentajeEfectivo).min().orElse(0);
        int max = lista.stream().mapToInt(ReglasOrden::porcentajeEfectivo).max().orElse(0);
        String nombre = d.getNombre() == null ? "" : d.getNombre();
        return nombre + (min == max ? " (" + max + ")%" : " (hasta " + max + ")%");
    }

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
            // Se consulta el detalle paginado/completo vía orden
            int totalRegistros = ordenProductoDAO.countByOrden(orden);
            List<OrdenProducto> productosOrden = ordenProductoDAO.findByOrden(orden, 0, Math.max(1, totalRegistros));

            for (OrdenProducto op : productosOrden) {
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

    public String descuentoAplicado(OrdenProducto op) {
        if (op == null || op.getPrecio() == null || op.getIdProducto() == null
                || op.getIdProducto().getPrecioSugerido() == null) {
            return "";
        }
        BigDecimal base = op.getIdProducto().getPrecioSugerido();
        if (base.signum() <= 0 || op.getPrecio().compareTo(base) >= 0) {
            return "";
        }
        BigDecimal ahorro = base.subtract(op.getPrecio());
        BigDecimal pct = ahorro.multiply(BigDecimal.valueOf(100))
                .divide(base, 0, RoundingMode.HALF_UP);
        return "-" + pct + "% (-$" + ahorro.setScale(2, RoundingMode.HALF_UP) + ")";
    }

    // ---------------- utilidades ----------------
    private boolean hayOrden() {
        return orden != null && orden.getIdOrden() != null;
    }

    private void mensaje(FacesMessage.Severity severidad, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, resumen, detalle));
    }

    public BigDecimal getTotal() {
        return hayOrden() ? ordenProductoDAO.totalByOrden(orden) : BigDecimal.ZERO;
    }

    // ---- getters y setters para la vista ----
    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
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
