package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.FacturaOrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasFactura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;

/**
 * Pestaña "Productos" de la factura (detalle). Mismo patrón que
 * OrdenProductoModel: @Dependent, sin @Named, inyectado en FacturaModel.
 */
@Dependent
public class FacturaOrdenProductoModel extends DefaultModel<FacturaOrdenProducto> implements Serializable {

    private static final long serialVersionUID = 1L;
    static final int MAX_ORDENES_DIALOGO = 50;

    @Inject
    private FacturaOrdenProductoDAO facturaOrdenProductoDAO;
    @Inject
    private OrdenProductoDAO ordenProductoDAO;
    @Inject
    private OrdenDAO ordenDAO;
    @Inject
    private Etiquetas etiquetas;

    private Factura factura;

    // Diálogo "Agregar orden completa"
    private List<SelectItem> opcionesOrden;
    private Orden ordenSeleccionada;

    @Override
    protected AbstractDataAccess<FacturaOrdenProducto> getDAO() {
        return facturaOrdenProductoDAO;
    }

    @Override
    protected UUID getId(FacturaOrdenProducto entidad) {
        return entidad.getIdFacturaOrdenProducto();
    }

    @Override
    protected FacturaOrdenProducto nuevoRegistro() {
        FacturaOrdenProducto nuevo = new FacturaOrdenProducto(UUID.randomUUID());
        nuevo.setIdFactura(factura);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Producto";
    }

    @Override
    protected List<FacturaOrdenProducto> cargarDatos(int first, int max) {
        if (!hayFactura()) {
            return Collections.emptyList();
        }
        return facturaOrdenProductoDAO.findByFactura(factura, first, max);
    }

    @Override
    protected int contarDatos() {
        if (!hayFactura()) {
            return 0;
        }
        return facturaOrdenProductoDAO.countByFactura(factura);
    }

    @Override
    protected String validarRegistro(FacturaOrdenProducto fop) {
        if (fop == null) {
            return "El registro no puede ser nulo";
        }
        if (fop.getIdFactura() == null || fop.getIdFactura().getIdFactura() == null) {
            return "Guarde primero la factura";
        }
        if (!ReglasFactura.esEditable(fop.getIdFactura())) {
            return "La factura está " + fop.getIdFactura().getEstado() + ": ya no admite cambios en sus productos";
        }
        if (fop.getIdOrdenProducto() == null || fop.getIdOrdenProducto().getIdOrdenProducto() == null) {
            return "Seleccione el producto de una orden";
        }
        // Al crear: el producto no puede estar ya en otra factura vigente.
        if (estado == ESTADO_CRUD.CREAR
                && facturaOrdenProductoDAO.countVigentesByOrdenProducto(fop.getIdOrdenProducto()) > 0) {
            return etiquetas.ordenProducto(fop.getIdOrdenProducto()) + " ya está facturado";
        }
        if (fop.getPrecio() == null || fop.getPrecio().signum() < 0) {
            return "El producto no tiene un precio válido";
        }
        return null;
    }

    @Override
    public void btnEliminarHandler() {
        if (registro != null && !ReglasFactura.esEditable(factura)) {
            notificar(FacesMessage.SEVERITY_ERROR, "El registro no fue eliminado",
                    "La factura ya no admite cambios en sus productos");
            return;
        }
        super.btnEliminarHandler();
    }

    /** Al elegir el producto de la orden se copia su precio (ya trae el descuento aplicado). */
    public void seleccionarOrdenProducto(OrdenProducto op) {
        if (registro != null && op != null) {
            registro.setIdOrdenProducto(op);
            registro.setPrecio(op.getPrecio());
        }
    }

    public List<OrdenProducto> completarOrdenProducto(String query) {
        try {
            return ordenProductoDAO.findPendientesDeFacturar(query, 10);
        } catch (RuntimeException ex) {
            notificarError("No se pudieron cargar los productos pendientes de facturar", ex);
            return new ArrayList<>();
        }
    }

    // ---------------- Agregar orden completa ----------------
    public void prepararOrdenes() {
        ordenSeleccionada = null;
        opcionesOrden = new ArrayList<>();
        try {
            for (Orden o : ordenDAO.findConPendientesDeFacturar(MAX_ORDENES_DIALOGO)) {
                opcionesOrden.add(new SelectItem(o, etiquetas.orden(o)));
            }
            if (opcionesOrden.isEmpty()) {
                notificar(FacesMessage.SEVERITY_WARN, "Sin órdenes pendientes",
                        "Todas las órdenes ya están facturadas");
            }
        } catch (RuntimeException ex) {
            notificarError("No se pudieron cargar las órdenes", ex);
        }
    }

    public void agregarOrden() {
        if (!hayFactura() || ordenSeleccionada == null) {
            notificar(FacesMessage.SEVERITY_ERROR, "No se agregó la orden", "Seleccione una orden");
            return;
        }
        if (!ReglasFactura.esEditable(factura)) {
            notificar(FacesMessage.SEVERITY_ERROR, "No se agregó la orden",
                    "La factura ya no admite cambios en sus productos");
            return;
        }
        try {
            int agregados = facturaOrdenProductoDAO.agregarPendientesDeOrden(factura, ordenSeleccionada);
            notificar(FacesMessage.SEVERITY_INFO, "Orden agregada",
                    agregados + " producto(s) de " + etiquetas.orden(ordenSeleccionada) + " se agregaron a la factura");
            ordenSeleccionada = null;
        } catch (RuntimeException ex) {
            notificarError("No se agregó la orden", ex);
        }
    }

    // ---------------- utilidades ----------------
    private boolean hayFactura() {
        return factura != null && factura.getIdFactura() != null;
    }

    public boolean isEditable() {
        return hayFactura() && ReglasFactura.esEditable(factura);
    }

    public BigDecimal getTotal() {
        try {
            return hayFactura() ? facturaOrdenProductoDAO.totalByFactura(factura) : BigDecimal.ZERO;
        } catch (RuntimeException ex) {
            notificarError("No se pudo calcular el total", ex);
            return BigDecimal.ZERO;
        }
    }

    // ---- getters y setters para la vista ----
    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public List<SelectItem> getOpcionesOrden() {
        return opcionesOrden;
    }

    public Orden getOrdenSeleccionada() {
        return ordenSeleccionada;
    }

    public void setOrdenSeleccionada(Orden ordenSeleccionada) {
        this.ordenSeleccionada = ordenSeleccionada;
    }
}
