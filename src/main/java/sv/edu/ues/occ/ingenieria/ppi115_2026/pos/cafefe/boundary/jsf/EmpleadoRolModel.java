package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.io.Serializable;
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

@Dependent
public class EmpleadoRolModel extends DefaultModel<EmpleadoRol> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmpleadoRolDAO empleadoRolDAO;

    @Inject
    private RolDAO rolDAO;

    private Empleado empleado;

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

    @Override
    protected List<EmpleadoRol> cargarDatos(int first, int max) {
        if (empleado == null || empleado.getIdEmpleado() == null) {
            return Collections.emptyList();
        }
        return empleadoRolDAO.findByEmpleado(empleado, first, max);
    }

    @Override
    protected int contarDatos() {
        if (empleado == null || empleado.getIdEmpleado() == null) {
            return 0;
        }
        return empleadoRolDAO.countByEmpleado(empleado);
    }

    @Override
    protected String validarRegistro(EmpleadoRol registro) {
        if (registro == null) {
            return "El registro no puede ser nulo";
        }
        if (registro.getIdEmpleado() == null || registro.getIdEmpleado().getIdEmpleado() == null) {
            return "Debe seleccionar un empleado válido";
        }
        if (registro.getIdRol() == null || registro.getIdRol().getIdRol() == null) {
            return "Debe seleccionar un rol válido";
        }
        if (registro.getIdRol().getActivo() != null && !registro.getIdRol().getActivo()) {
            return "No se puede asignar un rol inactivo";
        }
        if (estado == ESTADO_CRUD.CREAR && !Boolean.TRUE.equals(registro.getIdEmpleado().getActivo())) {
            return "No se puede asignar un rol a un empleado inactivo";
        }
        if (empleadoRolDAO.existeAsignacion(registro.getIdEmpleado(), registro.getIdRol(), registro.getIdEmpleadoRol())) {
            return "El empleado ya tiene asignado ese rol";
        }
        return null;
    }

    public List<Rol> completarRol(String query) {
        try {
            return rolDAO.buscarActivosPorNombre(query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los roles", ex);
            return new ArrayList<>();
        }
    }
    
    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }
}