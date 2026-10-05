package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;

/**
 *
 * @author hernandez
 */
@Named
@ViewScoped
public class EmpleadoModel extends DefaultModel<Empleado>{
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
