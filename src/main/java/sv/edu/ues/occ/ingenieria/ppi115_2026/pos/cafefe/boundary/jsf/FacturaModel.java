package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CajaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.FacturaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasFactura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 * Pantalla de Facturas (maestro). Sigue el mismo patrón que OrdenModel:
 * pestaña Generalidades para la factura y pestaña Productos para su detalle
 * (FacturaOrdenProductoModel, inyectado y @Dependent).
 */
@Named
@ViewScoped
public class FacturaModel extends DefaultModel<Factura> {

    private static final long serialVersionUID = 1L;

    @Inject
    private FacturaDAO facturaDAO;
    @Inject
    private CajaDAO cajaDAO;
    @Inject
    private EmpleadoRolDAO empleadoRolDAO;
    @Inject
    private RolDAO rolDAO;
    @Inject
    private FacturaOrdenProductoModel facturaOrdenProductoModel;
    @Inject
    private Etiquetas etiquetas;

    // Pestaña activa del p:tabView (0 = generalidades, 1 = productos).
    private int tabActiva;

    @Override
    protected AbstractDataAccess<Factura> getDAO() {
        return facturaDAO;
    }

    @Override
    protected UUID getId(Factura entidad) {
        return entidad.getIdFactura();
    }

    @Override
    protected Factura nuevoRegistro() {
        Factura nueva = new Factura(UUID.randomUUID());
        nueva.setFechaFacturacion(new Date());
        nueva.setEstado(ReglasFactura.PENDIENTE);
        return nueva;
    }

    @Override
    public String nombreBean() {
        return "Factura";
    }

    @Override
    protected void registroCambio() {
        facturaOrdenProductoModel.setFactura(registro);
        facturaOrdenProductoModel.btnCancelarHandler();
        tabActiva = 0;
    }

    @Override
    protected String validarRegistro(Factura f) {
        if (f == null) {
            return "El registro no puede ser nulo";
        }
        if (f.getIdEmpleadoRol() == null || f.getIdEmpleadoRol().getIdEmpleadoRol() == null) {
            return "Seleccione el empleado que factura";
        }
        if (!ReglasFactura.puedeFacturar(f.getIdEmpleadoRol())) {
            return etiquetas.empleadoRol(f.getIdEmpleadoRol())
                    + " no puede facturar: solo cajeros activos";
        }
        if (f.getIdCaja() == null || f.getIdCaja().getIdCaja() == null) {
            return "Seleccione la caja";
        }
        if (estado == ESTADO_CRUD.CREAR && !ReglasFactura.cajaDisponible(f.getIdCaja())) {
            return "La caja " + etiquetas.caja(f.getIdCaja()) + " está inactiva";
        }
        if (f.getFechaFacturacion() == null) {
            return "La fecha de facturación es obligatoria";
        }
        String guardado = estado == ESTADO_CRUD.CREAR ? null : estadoGuardado(f);
        if (!ReglasFactura.cambioDeEstadoPermitido(guardado, f.getEstado())) {
            return guardado == null
                    ? "Una factura nueva debe quedar PENDIENTE"
                    : "Una factura " + guardado + " ya no puede pasar a " + f.getEstado();
        }
        f.setCliente(ReglasFactura.normalizarCliente(f.getCliente()));
        return null;
    }

    /** Estado tal como está en la base (el de la pantalla puede venir modificado). */
    private String estadoGuardado(Factura f) {
        Factura enBase = facturaDAO.findById(f.getIdFactura());
        return enBase == null ? null : enBase.getEstado();
    }

    @Override
    public void btnCrearHandler() {
        Factura guardada = registro;
        super.btnCrearHandler();
        // Igual que Orden: al crear se queda abierta y salta a la pestaña de productos.
        if (registro == null && guardada != null) {
            setRegistro(guardada);
            estado = ESTADO_CRUD.MODIFICAR;
            tabActiva = 1;
        }
    }

    @Override
    public void btnEliminarHandler() {
        try {
            if (registro != null && !ReglasFactura.PENDIENTE.equals(estadoGuardado(registro))) {
                notificar(FacesMessage.SEVERITY_ERROR, "El registro no fue eliminado",
                        "Solo se puede eliminar una factura PENDIENTE; una pagada no se toca y una errónea se anula");
                return;
            }
        } catch (RuntimeException ex) {
            notificarError("El registro no fue eliminado", ex);
            return;
        }
        super.btnEliminarHandler();
    }

    public List<EmpleadoRol> completarEmpleadoRol(String query) {
        try {
            // Los roles se filtran en Java (pocos y con tildes); los empleados, en la consulta.
            List<Rol> roles = rolDAO.findActivos().stream().filter(ReglasFactura::rolAdmitido).toList();
            return empleadoRolDAO.findActivosPorRolesYNombre(roles, query, 10);
        } catch (RuntimeException ex) {
            notificarError("No se pudieron cargar los empleados que pueden facturar", ex);
            return new ArrayList<>();
        }
    }

    public List<Caja> completarCaja(String query) {
        try {
            return cajaDAO.buscarActivasPorNombre(query, 10);
        } catch (RuntimeException ex) {
            notificarError("No se pudieron cargar las cajas", ex);
            return new ArrayList<>();
        }
    }

    /** Estados que se pueden elegir. Si ya está PAGADA, solo se muestra ese (lo maneja el cobro). */
    public List<String> getEstados() {
        if (registro != null && ReglasFactura.PAGADA.equals(registro.getEstado())) {
            return List.of(ReglasFactura.PAGADA);
        }
        return ReglasFactura.ESTADOS_EDITABLES;
    }

    /** El estado solo se cambia al editar una factura pendiente (al crear siempre es PENDIENTE). */
    public boolean isEstadoEditable() {
        return estado == ESTADO_CRUD.MODIFICAR && ReglasFactura.esEditable(registro);
    }

    public String formatoFecha(Date fecha) {
        return fecha == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fecha);
    }

    // ---- getters y setters para la vista ----
    public FacturaOrdenProductoModel getFacturaOrdenProductoModel() {
        return facturaOrdenProductoModel;
    }

    public int getTabActiva() {
        return tabActiva;
    }

    public void setTabActiva(int tabActiva) {
        this.tabActiva = tabActiva;
    }
}
