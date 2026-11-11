package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CajaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;

/**
 * Mantenimiento de Cajas (CRUD simple, mismo patrón que RolModel).
 * Una caja con facturas no se elimina: se desactiva, y deja de aparecer
 * al crear facturas nuevas.
 */
@Named
@ViewScoped
public class CajaModel extends DefaultModel<Caja> {

    private static final long serialVersionUID = 1L;

    @Inject
    private CajaDAO cajaDAO;

    @Override
    protected AbstractDataAccess<Caja> getDAO() {
        return cajaDAO;
    }

    @Override
    protected UUID getId(Caja entidad) {
        return entidad.getIdCaja();
    }

    @Override
    protected Caja nuevoRegistro() {
        Caja nueva = new Caja(UUID.randomUUID());
        nueva.setActivo(true);
        return nueva;
    }

    @Override
    public String nombreBean() {
        return "Caja";
    }

    @Override
    protected String validarRegistro(Caja caja) {
        if (caja == null) {
            return "El registro no puede ser nulo";
        }
        if (caja.getNombre() == null || caja.getNombre().isBlank()) {
            return "El nombre de la caja es obligatorio";
        }
        caja.setNombre(caja.getNombre().trim());
        if (cajaDAO.existeNombre(caja.getNombre(), caja.getIdCaja())) {
            return "Ya existe una caja llamada " + caja.getNombre();
        }
        return null;
    }

    @Override
    protected List<Caja> cargarDatos(int first, int max) {
        return cajaDAO.findRange(first, max);
    }

    @Override
    protected int contarDatos() {
        return (int) Math.min(cajaDAO.count(), Integer.MAX_VALUE);
    }
}
