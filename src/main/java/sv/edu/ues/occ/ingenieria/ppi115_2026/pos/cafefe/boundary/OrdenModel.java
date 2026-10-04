package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasOrden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;

/**
 *
 * @author hernandez
 */
@Named
@ViewScoped
public class OrdenModel extends DefaultModel<Orden> {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenDAO ordenDAO;

    @Inject
    private OrdenProductoModel ordenProductoModel;

    @Inject
    private Etiquetas etiquetas;

    // Combo "Empleado": solo quienes pueden tomar órdenes
    private List<SelectItem> opcionesEmpleado;

    // Pestaña activa del p:tabView (0 = generalidades, 1 = productos).
    private int tabActiva;

    @Override
    protected AbstractDataAccess<Orden> getDAO() {
        return ordenDAO;
    }

    @Override
    protected UUID getId(Orden entidad) {
        return entidad.getIdOrden();
    }

    @Override
    protected Orden nuevoRegistro() {
        Orden nueva = new Orden(UUID.randomUUID());
        nueva.setFechaCreacion(new Date()); // por defecto, ahora mismo
        return nueva;
    }

    @Override
    public String nombreBean() {
        return "Orden";
    }

    @Override
    protected List<Orden> cargarDatos(int first, int max) {
        return ordenDAO.findRangeOrdenado(first, max);
    }

    @Override
    protected void cargarOpciones() {
        opcionesEmpleado = new ArrayList<>();
        for (EmpleadoRol er : ordenDAO.findEmpleadoRolActivos()) {
            if (ReglasOrden.puedeTomarOrdenes(er)) {
                opcionesEmpleado.add(new SelectItem(er, etiquetas.empleadoRol(er)));
            }
        }
    }

    @Override
    protected void registroCambio() {
        // Si la orden guardada tiene un empleado que ya no está en el combo
        // (lo desactivaron o le quitaron el rol), se agrega para poder verlo;
        // al guardar, validar() pedirá elegir uno admitido.
        if (registro != null && registro.getIdEmpleadoRol() != null && opcionesEmpleado != null
                && opcionesEmpleado.stream().noneMatch(o -> registro.getIdEmpleadoRol().equals(o.getValue()))) {
            opcionesEmpleado.add(new SelectItem(registro.getIdEmpleadoRol(),
                    etiquetas.empleadoRol(registro.getIdEmpleadoRol()) + " - no admitido"));
        }
        ordenProductoModel.setOrden(registro);
        ordenProductoModel.btnCancelarHandler();
        tabActiva = 0;
    }

    /**
     * Reglas de la orden antes de guardar. Devuelve el mensaje de error, o
     * null si todo está bien.
     */
    String validar(Orden orden) {
        if (orden.getIdEmpleadoRol() == null) {
            return "Seleccione el empleado que toma la orden";
        }
        if (!ReglasOrden.puedeTomarOrdenes(orden.getIdEmpleadoRol())) {
            return etiquetas.empleadoRol(orden.getIdEmpleadoRol())
                    + " no puede tomar órdenes: solo camareros, atención al cliente o baristas activos";
        }
        if (orden.getFechaCreacion() == null) {
            return "La fecha de la orden es obligatoria";
        }
        return null;
    }

    /**
     * Crea la orden y, si salió bien, la deja abierta en la pestaña
     * "Productos" para seguir agregando sin buscarla en la tabla.
     */
    @Override
    public void btnCrearHandler() {
        if (registro != null && !validacionCorrecta()) {
            return;
        }
        Orden guardada = registro;
        super.btnCrearHandler();
        if (registro == null && guardada != null) { // null = se creó con éxito
            setRegistro(guardada);
            estado = ESTADO_CRUD.MODIFICAR;
            tabActiva = 1;
        }
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
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Orden no guardada", error));
            return false;
        }
        return true;
    }

    /** Fecha legible para la tabla (zona horaria del servidor, igual que el p:datePicker). */
    public String formatoFecha(Date fecha) {
        return fecha == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fecha);
    }

    // ---- getters y setters para la vista ----

    public OrdenProductoModel getOrdenProductoModel() {
        return ordenProductoModel;
    }

    public List<SelectItem> getOpcionesEmpleado() {
        return opcionesEmpleado;
    }

    public int getTabActiva() {
        return tabActiva;
    }

    public void setTabActiva(int tabActiva) {
        this.tabActiva = tabActiva;
    }
}
