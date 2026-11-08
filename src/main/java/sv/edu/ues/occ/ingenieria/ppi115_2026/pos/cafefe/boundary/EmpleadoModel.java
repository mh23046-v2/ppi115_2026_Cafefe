package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;

@Named
@ViewScoped
public class EmpleadoModel extends DefaultModel<Empleado> {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmpleadoDAO empleadoDAO;

    @Inject
    private EmpleadoRolModel empleadoRolModel;

    // Pestaña activa del p:tabView (0 = datos, 1 = roles).
    private int tabActiva;

    @Override
    protected AbstractDataAccess<Empleado> getDAO() {
        return empleadoDAO;
    }

    @Override
    protected UUID getId(Empleado entidad) {
        return entidad.getIdEmpleado();
    }

    @Override
    protected Empleado nuevoRegistro() {
        Empleado nuevo = new Empleado(UUID.randomUUID());
        nuevo.setActivo(true);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Empleado";
    }

    @Override
    protected void registroCambio() {
        empleadoRolModel.setEmpleado(registro);
        empleadoRolModel.btnCancelarHandler();
        tabActiva = 0;
    }

    @Override
    protected String validarRegistro(Empleado registro) {
        if (registro == null) {
            return "El registro no puede ser nulo";
        }
        if (registro.getNombre() == null || registro.getNombre().isBlank()) {
            return "El nombre del empleado es obligatorio";
        }
        if (registro.getApellido() == null || registro.getApellido().isBlank()) {
            return "El apellido del empleado es obligatorio";
        }
        return null;
    }

    @Override
    protected List<Empleado> cargarDatos(int first, int max) {
        return empleadoDAO.findByNombre("", first, max);
    }

    @Override
    protected int contarDatos() {
        Long total = empleadoDAO.countByNombre("");
        return total != null ? total.intValue() : 0;
    }

    public List<Empleado> completarEmpleado(String query) {
        try {
            return empleadoDAO.findByNombre(query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los empleados", ex);
            return new ArrayList<>();
        }
    }

    public EmpleadoRolModel getEmpleadoRolModel() {
        return empleadoRolModel;
    }

    public int getTabActiva() {
        return tabActiva;
    }

    public void setTabActiva(int tabActiva) {
        this.tabActiva = tabActiva;
    }
}