package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasOrden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;

@Named
@ViewScoped
public class OrdenModel extends DefaultModel<Orden> {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenDAO ordenDAO;

    @Inject
    private EmpleadoRolDAO empleadoRolDAO;

    @Inject
    private OrdenProductoModel ordenProductoModel;

    @Inject
    private Etiquetas etiquetas;

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
        nueva.setFechaCreacion(new Date());
        return nueva;
    }

    @Override
    public String nombreBean() {
        return "Orden";
    }

    @Override
    protected List<Orden> cargarDatos(int first, int max) {
        return ordenDAO.findRange(first, max);
    }

    @Override
    protected int contarDatos() {
        return ordenDAO.countAll();
    }

    @Override
    protected void registroCambio() {
        ordenProductoModel.setOrden(registro);
        ordenProductoModel.btnCancelarHandler();
        tabActiva = 0;
    }

    @Override
    protected String validarRegistro(Orden registro) {
        if (registro == null) {
            return "El registro no puede ser nulo";
        }
        if (registro.getIdEmpleadoRol() == null || registro.getIdEmpleadoRol().getIdEmpleadoRol() == null) {
            return "Seleccione el empleado que toma la orden";
        }
        if (!ReglasOrden.puedeTomarOrdenes(registro.getIdEmpleadoRol())) {
            return etiquetas.empleadoRol(registro.getIdEmpleadoRol())
                    + " no puede tomar órdenes: solo camareros, atención al cliente o baristas activos";
        }
        if (registro.getFechaCreacion() == null) {
            return "La fecha de la orden es obligatoria";
        }
        return null;
    }

    public List<EmpleadoRol> completarEmpleadoRol(String query) {
        try {
            // Consulta los roles asignados cuyo empleado coincida con la búsqueda (máximo 15 para margen de filtrado por reglas)
            List<EmpleadoRol> resultados = empleadoRolDAO.findByEmpleadoNombre(query, 15);
            return resultados.stream()
                    .filter(ReglasOrden::puedeTomarOrdenes)
                    .limit(10)
                    .toList();
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los empleados aptos para tomar la orden", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public void btnCrearHandler() {
        Orden guardada = registro;
        super.btnCrearHandler();
        if (registro == null && guardada != null) {
            setRegistro(guardada);
            estado = ESTADO_CRUD.MODIFICAR;
            tabActiva = 1;
        }
    }

    public String formatoFecha(Date fecha) {
        return fecha == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fecha);
    }

    // ---- getters y setters para la vista ----
    public OrdenProductoModel getOrdenProductoModel() {
        return ordenProductoModel;
    }

    public int getTabActiva() {
        return tabActiva;
    }

    public void setTabActiva(int tabActiva) {
        this.tabActiva = tabActiva;
    }
}
