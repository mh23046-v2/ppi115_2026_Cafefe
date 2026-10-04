package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 *
 * @author hernandez
 */
// Es dependiente porque existe dentro de la pagina de Empleado
@Dependent
public class EmpleadoRolModel extends DefaultModel<EmpleadoRol>{

    private static final long serialVersionUID = 1L;

    @Inject
    private EmpleadoRolDAO empleadoRolDAO;
    @Inject
    private RolDAO rolDAO;
    @Inject
    private Etiquetas etiquetas;
    
    private Empleado empleado;
    private List<SelectItem> opcionesIdRol;
    
    @Override
    protected AbstractDataAccess<EmpleadoRol> getDAO() {
        return empleadoRolDAO;
    }

    @Override
    protected UUID getId(EmpleadoRol entidad) {
        return entidad.getIdEmpleadoRol();
    }

    @Override
    protected EmpleadoRol nuevoRegistro() {
        EmpleadoRol nuevo = new EmpleadoRol(UUID.randomUUID());
        nuevo.setIdEmpleado(empleado);
        nuevo.setActivo(true);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Rol de Empleado";
    }
    
    //Solo cargamos rol del empleado seleccionado
    @Override
    protected List<EmpleadoRol> cargarDatos(int first, int max) {
        if (empleado == null || empleado.getIdEmpleado() == null) {
            return Collections.emptyList();
        }
        return empleadoRolDAO.findByIdEmpleado(empleado.getIdEmpleado(), first, max);
    }

    @Override
    protected int contarDatos() {
        if (empleado == null || empleado.getIdEmpleado() == null) {
            return 0;
        }
        return (int) empleadoRolDAO.countByIdEmpleado(empleado.getIdEmpleado());
    }

    /** Combo de roles (el empleado no se elige: es el del maestro). */
   @Override
    protected void cargarOpciones() {
        opcionesIdRol = new ArrayList<>();
        for (Rol x : rolDAO.findAll()) {
            opcionesIdRol.add(new SelectItem(x, etiquetas.rol(x), null, !x.getActivo()));
        }   
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public List<SelectItem> getOpcionesIdRol() {
        return opcionesIdRol;
    }
    
}
