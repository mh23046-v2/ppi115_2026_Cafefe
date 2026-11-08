package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

@Named
@ViewScoped
public class RolModel extends DefaultModel<Rol> {

    private static final long serialVersionUID = 1L;

    @Inject
    private RolDAO rolDAO;

    @Override
    protected AbstractDataAccess<Rol> getDAO() {
        return rolDAO;
    }

    @Override
    protected UUID getId(Rol entidad) {
        return entidad.getIdRol();
    }

    @Override
    protected Rol nuevoRegistro() {
        Rol nuevo = new Rol(UUID.randomUUID());
        nuevo.setActivo(true);
        return nuevo;
    }

    @Override
    public String nombreBean() {
        return "Rol";
    }

    @Override
    protected String validarRegistro(Rol registro) {
        if (registro == null) {
            return "El registro no puede ser nulo";
        }
        if (registro.getNombre() == null || registro.getNombre().isBlank()) {
            return "El nombre del rol es obligatorio";
        }
        return null;
    }

    @Override
    protected List<Rol> cargarDatos(int first, int max) {
        return rolDAO.findByNombre("", first, max);
    }

    @Override
    protected int contarDatos() {
        Long total = rolDAO.countByNombre("");
        return total != null ? total.intValue() : 0;
    }

    // Método callback para cuando EmpleadoRol use este model en autocompletado
    public List<Rol> completarRol(String query) {
        try {
            return rolDAO.findByNombre(query, 10);
        } catch (Exception ex) {
            notificarError("No se pudieron cargar los roles", ex);
            return new ArrayList<>();
        }
    }
}