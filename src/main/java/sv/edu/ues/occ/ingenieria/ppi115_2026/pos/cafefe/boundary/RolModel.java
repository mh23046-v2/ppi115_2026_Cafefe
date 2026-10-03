package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 *
 * @author hernandez
 */
@Named
@ViewScoped
public class RolModel extends DefaultModel<Rol>{

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
    
}
